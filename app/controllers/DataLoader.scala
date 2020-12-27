package controllers
import javax.inject.Inject

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Source
import akka.util.ByteString

import scala.collection.immutable
import scala.concurrent.ExecutionContext

case class Parameter(length: Int)

class DataLoader @Inject() (client: ApiClient) {

  def load(from: Int, to: Int)(implicit
      ec: ExecutionContext
  ): Source[ByteString, NotUsed] = {

    val source = Source((from to to).map(Parameter.apply))

    // 外部 API を非同期で順々に呼び出す
    val toResponse = Flow[Parameter].mapAsync(1) { param =>
      println(param)
      client.execute(param).map(response => param -> response)
    }

    // データをCSV形式に変換する
    val toCsv = Flow[(Parameter, Response)]
      .prefixAndTail(1) // 先頭1要素とそれ以外に分ける
      .flatMapConcat { case (Seq((parameter, response)), rest) =>
        Source("長さ,値" +: parameterToCsv(parameter, response))
          .concat(rest.mapConcat { case (p, r) => parameterToCsv(p, r) })
      }

    // 各行末に改行文字を付ける
    val appendLineSeparator = Flow[String].map(_ + "\r\n")

    // 文字列をバイト列に変換する。Charsetを指定しない場合はUTF-8。
    // なお、ByteStringはバイト列であって、文字列（String）とは関係ない。
    val toByteString = Flow[String].map(ByteString(_))

    // 先頭にUTF-8のBOMを付ける
    val addBom =
      Flow[ByteString].prepend(Source.single(ByteString(0xef, 0xbb, 0xbf)))

    source via toResponse via toCsv via appendLineSeparator via toByteString via addBom
  }

  private def parameterToCsv(parameter: Parameter, response: Response) =
    immutable.Seq(
      s"${parameter.length},${response.hiragana}",
      s"${parameter.length},${response.katakana}"
    )
}
