package devkit

import sbt.PlayExceptions.AssetCompilationException
import java.io._
import play.api._
import scalax.file._

object SingleJavascriptCompiler {

  import com.google.javascript.jscomp.{ Compiler, CompilerOptions, JSSourceFile, CompilationLevel }

  /**
   * Compile a JS file with its dependencies
   * @return a triple containing the original source code, the minified source code, the list of dependencies (including the input file)
   * @param source
   * @param simpleCompilerOptions user supplied simple command line parameters
   * @param fullCompilerOptions user supplied full blown CompilerOptions instance
   */
  def compile(source: File, simpleCompilerOptions: Seq[String], fullCompilerOptions: Option[CompilerOptions]): (String, Option[String], Seq[File]) = {
    import scala.util.control.Exception._

    val origin = Path(source).string

    val options = fullCompilerOptions.getOrElse {
      val defaultOptions = new CompilerOptions()
      defaultOptions.closurePass = true

      simpleCompilerOptions.foreach(_ match {
        case "advancedOptimizations" => CompilationLevel.ADVANCED_OPTIMIZATIONS.setOptionsForCompilationLevel(defaultOptions)
        case "checkCaja" => defaultOptions.setCheckCaja(true)
        case "checkControlStructures" => defaultOptions.setCheckControlStructures(true)
        case "checkTypes" => defaultOptions.setCheckTypes(true)
        case "checkSymbols" => defaultOptions.setCheckSymbols(true)
        case _ => Unit // Unknown option
      })
      defaultOptions
    }

    val compiler = new Compiler()

    val input = Array(JSSourceFile.fromFile(source))

    catching(classOf[Exception]).either(compiler.compile(Array[JSSourceFile](), input, options).success) match {
      case Right(true) => (origin, Some(compiler.toSource()), Nil)
      case Right(false) => {
        val error = compiler.getErrors().head
        val errorFile = Some(source)
        throw AssetCompilationException(errorFile, error.description, Some(error.lineNumber), None)
      }
      case Left(exception) =>
        exception.printStackTrace()
        throw AssetCompilationException(Some(source), "Internal Closure Compiler error (see logs)", None, None)
    }
  }

  /**
   * Minify a Javascript string
   */
  def minify(source: String, name: Option[String]): String = {

    val compiler = new Compiler()
    val options = new CompilerOptions()

    val input = Array[JSSourceFile](JSSourceFile.fromCode(name.getOrElse("unknown"), source))

    compiler.compile(Array[JSSourceFile](), input, options).success match {
      case true => compiler.toSource()
      case false => {
        val error = compiler.getErrors().head
        throw AssetCompilationException(None, error.description, Some(error.lineNumber), None)
      }
    }
  }

  case class CompilationException(message: String, jsFile: File, atLine: Option[Int]) extends PlayException.ExceptionSource(
    "JS Compilation error", message) {
    def line = atLine.map(_.asInstanceOf[java.lang.Integer]).orNull
    def position = null
    def input = scalax.file.Path(jsFile).string
    def sourceName = jsFile.getAbsolutePath
  }

  /*
   * execute a native compiler for given command
   */
  def executeNativeCompiler(in: String, source: File): String = {
    import scala.sys.process._
    val qb = Process(in)
    var out = List[String]()
    var err = List[String]()
    val exit = qb ! ProcessLogger((s) => out ::= s, (s) => err ::= s)
    if (exit != 0) {
      val eRegex = """.*Parse error on line (\d+):.*""".r
      val errReverse = err.reverse
      val r = eRegex.unapplySeq(errReverse.mkString("")).map(_.head.toInt)
      val error = "error in: " + in + " \n" + errReverse.mkString("\n")

      throw CompilationException(error, source, r)
    }
    out.reverse.mkString("\n")
  }

}