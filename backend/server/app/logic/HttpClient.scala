package logic

import java.io.{ByteArrayInputStream, InputStream}
import java.net.URLEncoder

import com.ning.http.client.AsyncHttpClientConfig.Builder
import com.ning.http.client._
import utils.Implicits._

import scala.concurrent.Future

/**
  * @author Jenda Kolena, kolena@avast.com
  */
object HttpClient {
  protected val builder = new Builder()

  protected val client = new AsyncHttpClient(builder.build())

  def get(url: String, params: (String, String)*): Future[HttpResponse] = {
    val b = client.prepareGet(url)
    params.foreach { case (key, value) => b.addQueryParam(key, value) }

    b.execute()
  }

  def post(url: String, body: Array[Byte]): Future[HttpResponse] = {
    val b = client.preparePost(url)

    b.setBody(body)

    b.execute()
  }
}

case class HttpResponse(payload: Array[Byte]) {
  def asStream: InputStream = new ByteArrayInputStream(payload)
}