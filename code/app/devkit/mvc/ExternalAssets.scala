package devkit.mvc

import play.api._
import play.api.mvc._

import Play.current

import java.io._
import play.api._
import play.api.mvc._
import play.api.libs._
import play.api.libs.iteratee._

import Play.current

import java.io._
import java.net.JarURLConnection
import scalax.io.{ Resource }
import org.joda.time.format.{ DateTimeFormatter, DateTimeFormat }
import org.joda.time.DateTimeZone
import collection.JavaConverters._
import scala.util.control.NonFatal
/**
 * Controller that serves static resources from an external folder.
 * All assets are served with max-age=3600 cache directive.
 *
 * You can use this controller in any application, usage example :
 *
 * {{{
 * public static play.api.mvc.Action<AnyContent> external(String path){
 *      File appsFolder = new File(Files.applicationRoot().getParent());
 *
 *      return ExternalAsset.at(appsFolder, path);
 *  }
 *
 * }}}
 *
 */
object ExternalAssets extends play.api.mvc.Controller {

  private val timeZoneCode = "GMT"

  //Dateformatter is immutable and threadsafe
  private val df: DateTimeFormatter =
    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss '" + timeZoneCode + "'").withLocale(java.util.Locale.ENGLISH).withZone(DateTimeZone.forID(timeZoneCode))

  //Dateformatter is immutable and threadsafe
  private val dfp: DateTimeFormatter =
    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss").withLocale(java.util.Locale.ENGLISH).withZone(DateTimeZone.forID(timeZoneCode))

  private val parsableTimezoneCode = " " + timeZoneCode

  private lazy val defaultCharSet = Play.configuration.getString("default.charset").getOrElse("utf-8")

  private def addCharsetIfNeeded(mimeType: String): String =
    if (MimeTypes.isText(mimeType))
      "; charset=" + defaultCharSet
    else ""

  /**
   * Generates an `Action` that serves a static resource from an external folder
   *
   * @param folder the root folder for searching the static resource files such as `"/home/peter/public"`, `C:\external`
   * @param file the file part extracted from the URL
   */
  def at(folder : File, file: String): Action[AnyContent] = Action { request =>



    def parseDate(date: String): Option[java.util.Date] = try {
      //jodatime does not parse timezones, so we handle that manually
      val d = dfp.parseDateTime(date.replace(parsableTimezoneCode, "")).toDate
      Some(d)
    } catch {
      case NonFatal(_) => None
    }

    val resourceName = Option(file).map(name => if (name.startsWith("/")) name else ("/" + name)).get

    if (new File(resourceName).isDirectory || !new File(folder, resourceName).getCanonicalPath.startsWith(folder.getCanonicalPath)) {
      NotFound
    } else {

      val gzippedResource = Play.resource(resourceName + ".gz")

      val resource = {
        gzippedResource.map(_ -> true)
          .filter(_ => request.headers.get(ACCEPT_ENCODING).map(_.split(',').exists(_ == "gzip" && Play.isProd)).getOrElse(false))
          .orElse(Option( new File(folder, resourceName).toURI.toURL ).map(_ -> false))
      }
      resource.map {

        case (url, _) if (new File(url.getFile).isDirectory ||  !new File(url.getFile).exists)=> NotFound

        case (url, isGzipped) => {

          lazy val (length, resourceData) = {
            val stream = url.openStream()
            try {
              (stream.available, Enumerator.fromStream(stream))
            } catch {
              case _ => (-1, Enumerator[Array[Byte]]())
            }
          }

          if (length == -1) {
            NotFound
          } else {
            request.headers.get(IF_NONE_MATCH).flatMap { ifNoneMatch =>
              etagFor(url).filter(_ == ifNoneMatch)
            }.map(_ => NotModified).getOrElse {
              request.headers.get(IF_MODIFIED_SINCE).flatMap(parseDate).flatMap { ifModifiedSince =>
                lastModifiedFor(url).flatMap(parseDate).filterNot(lastModified => lastModified.after(ifModifiedSince))
              }.map(_ => NotModified.withHeaders(
                DATE -> df.print({ new java.util.Date }.getTime))).getOrElse {

                // Prepare a streamed response
                val response = SimpleResult(
                  header = ResponseHeader(OK, Map(
                    CONTENT_LENGTH -> length.toString,
                    CONTENT_TYPE -> MimeTypes.forFileName(file).map(m => m + addCharsetIfNeeded(m)).getOrElse(BINARY),
                    DATE -> df.print({ new java.util.Date }.getTime))),
                  resourceData)

                // If there is a gzipped version, even if the client isn't accepting gzip, we need to specify the
                // Vary header so proxy servers will cache both the gzip and the non gzipped version
                val gzippedResponse = (gzippedResource.isDefined, isGzipped) match {
                  case (true, true) => response.withHeaders(VARY -> ACCEPT_ENCODING, CONTENT_ENCODING -> "gzip")
                  case (true, false) => response.withHeaders(VARY -> ACCEPT_ENCODING)
                  case _ => response
                }

                // Add Etag if we are able to compute it
                val taggedResponse = etagFor(url).map(etag => gzippedResponse.withHeaders(ETAG -> etag)).getOrElse(gzippedResponse)
                val lastModifiedResponse = lastModifiedFor(url).map(lastModified => taggedResponse.withHeaders(LAST_MODIFIED -> lastModified)).getOrElse(taggedResponse)

                // Add Cache directive if configured
                val cachedResponse = lastModifiedResponse.withHeaders(CACHE_CONTROL -> {
                  Play.configuration.getString("\"assets.cache." + resourceName + "\"").getOrElse(Play.mode match {
                    case Mode.Prod => Play.configuration.getString("assets.defaultCache").getOrElse("max-age=3600")
                    case _ => "no-cache"
                  })
                })

                cachedResponse

              }: Result

            }

          }

        }

      }.getOrElse(NotFound)

    }

  }

  private val lastModifieds = (new java.util.concurrent.ConcurrentHashMap[String, String]()).asScala

  private def lastModifiedFor(resource: java.net.URL): Option[String] = {
    lastModifieds.get(resource.toExternalForm).filter(_ => Play.isProd).orElse {
      val maybeLastModified = resource.getProtocol match {
        case "file" => Some(df.print({ new java.util.Date(new java.io.File(resource.getPath).lastModified).getTime }))
        case "jar" => {
          resource.getPath.split('!').drop(1).headOption.flatMap { fileNameInJar =>
            Option(resource.openConnection)
              .collect { case c: JarURLConnection => c }
              .flatMap(c => Option(c.getJarFile.getJarEntry(fileNameInJar.drop(1))))
              .map(_.getTime)
              .filterNot(_ == 0)
              .map(lastModified => df.print({ new java.util.Date(lastModified) }.getTime))
          }
        }
        case _ => None
      }
      maybeLastModified.foreach(lastModifieds.put(resource.toExternalForm, _))
      maybeLastModified
    }
  }

  // -- ETags handling

  private val etags = (new java.util.concurrent.ConcurrentHashMap[String, String]()).asScala

  private def etagFor(resource: java.net.URL): Option[String] = {
    etags.get(resource.toExternalForm).filter(_ => Play.isProd).orElse {
      val maybeEtag = lastModifiedFor(resource).map(_ + " -> " + resource.toExternalForm).map("\"" + Codecs.sha1(_) + "\"")
      maybeEtag.foreach(etags.put(resource.toExternalForm, _))
      maybeEtag
    }
  }

}


