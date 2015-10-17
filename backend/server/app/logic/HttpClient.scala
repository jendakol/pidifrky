package logic

import java.io.InputStream

import com.google.common.io.ByteStreams
import com.ning.http.client.AsyncHttpClientConfig.Builder
import com.ning.http.client._
import utils.Implicits._
import utils.Logging

import scala.concurrent.Future

/**
  * @author Jenda Kolena, kolena@avast.com
  */
object HttpClient extends Logging {
  protected val builder = new Builder().setConnectTimeout(10000)

  protected val client = new AsyncHttpClient(builder.build())

  def get(url: String, params: (String, String)*): Future[HttpResponse] = {
    Logger.debug(s"HTTP GET to $url ; ${if (params.nonEmpty) params.map { case (key, value) => s"$key -> $value" }.mkString("params = [", ", ", "]") else "no params"}")

    val b = client.prepareGet(url)
    params.foreach { case (key, value) => b.addQueryParam(key, value) }

    b.execute()
  }

  def post(url: String, body: Array[Byte]): Future[HttpResponse] = {
    Logger.debug("HTTP POST to " + url)

    val b = client.preparePost(url)

    b.setBody(body)

    b.execute()
  }
}

case class HttpResponse(stream: InputStream, contentLength: Option[Long]) {
  def asString: String = new String(ByteStreams.toByteArray(stream))
}