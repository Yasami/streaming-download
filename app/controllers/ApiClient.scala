package controllers
import javax.inject.Inject

import com.typesafe.scalalogging.StrictLogging
import play.api.http.Status
import play.api.libs.json.Format
import play.api.libs.json.JsError
import play.api.libs.json.JsSuccess
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

case class Response(hiragana: String, katakana: String)

object Response {
  implicit val format: Format[Response] = Json.format[Response]
}

class ApiClient @Inject() (wsClient: WSClient) extends StrictLogging {
  def execute(parameter: Parameter)(implicit ec: ExecutionContext): Future[Response] = {
    logger.info("execute")
    val result = wsClient
      .url("http://localhost:9000/random")
      .addQueryStringParameters("length" -> parameter.length.toString)
      .get()
      .map { response =>
        response.status match {
          case Status.OK =>
            logger.info("OK")
            response
          case other =>
            val message = s"Status: $other"
            logger.error(message)
            throw new Exception(message)
        }
      }
      .map { response =>
        response.body[JsValue].validate[Response] match {
          case JsSuccess(value, _) =>
            value
          case JsError(errors) =>
            val message = errors
              .map {
                case (path, validationErrors) =>
                  s"$path(${validationErrors.mkString(", ")})"
              }
              .mkString(", ")
            throw new Exception(s"Json validation is failed: [$message]")
        }
      }
    result
  }
}
