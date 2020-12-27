import play.sbt.PlayRunHook
import sbt._
import scala.sys.process.Process

object RunHook {
  def apply(base: File): PlayRunHook = {

    object YarnBuild extends PlayRunHook {

//      var process: Option[Process] = None

      override def beforeStarted(): Unit = {
        if (!(base / "frontend" / "node_modules").exists())
          Process("yarn install", base / "frontend").!
        Process("yarn build", base / "frontend").!
      }

//      override def afterStarted(): Unit =
//        process = Option(Process("yarn start", base / "frontend").run)
//
//      override def afterStopped(): Unit = {
//        process.foreach(_.destroy())
//        process = None
//      }
    }

    YarnBuild
  }
}
