package controllers

import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

import play.api.libs.json.Json
import play.api.mvc._

import scala.collection.immutable.List
import scala.concurrent.ExecutionContext
import scala.util.Random

@Singleton
class DownloadController @Inject() (
    cc: ControllerComponents,
    loader: DataLoader
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def index(from: Int, to: Int): Action[AnyContent] = Action { request =>
    println("index")
    if (request.headers.get("apikey").contains("abcd")) {
      Ok.chunked(
        loader.load(from, to),
        //None,
        inline = false,
        Some("random.csv")
      )
    } else {
      Unauthorized
    }
  }

  def random(length: Int): Action[AnyContent] = Action {
    length match {
      case i if i < 10 =>
        val error = Json.obj(
          "title" -> "Too short length ",
          "invalid-params" -> Seq(
            Json
              .obj(
                "name" -> "length",
                "reason" -> "must be greater than or equal to 10"
              )
          )
        )
        BadRequest(Json.toJson(error))
      case _ =>
        // データ生成に時間がかかる様子を表現
        TimeUnit.SECONDS.sleep(10)

        def randomString(low: Char, high: Char, length: Int) =
          List.fill(length)((Random.nextInt(high - low) + low).toChar).mkString

        val js = Json.obj(
          "hiragana" -> randomString('あ', 'ん', length),
          "katakana" -> randomString('ア', 'ン', length)
        )
        Ok(js)
    }
  }
}
