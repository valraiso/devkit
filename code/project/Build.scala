import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "devkit"
  val appVersion      = "1.1"

  val appDependencies = Seq(
    // Add your project dependencies here,
    javaCore
  )

  val valraisoReleases   = Resolver.file("file", new File(Path.userHome.absolutePath+"/dev/apps/valraiso.github.com/releases/"))
  val valraisoSnapshots  = Resolver.file("file", new File(Path.userHome.absolutePath+"/dev/apps/valraiso.github.com/snapshot/"))
  val valraisoRepository = if(appVersion.endsWith("SNAPSHOT")) valraisoSnapshots else valraisoReleases


  val main = play.Project(appName, appVersion, appDependencies).settings(
    organization := "valraiso",
    //publishMavenStyle := true,
    playPlugin:=true,
    publishTo := Some(valraisoRepository)
  )

}
