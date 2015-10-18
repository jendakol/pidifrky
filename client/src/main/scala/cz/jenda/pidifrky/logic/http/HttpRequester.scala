package cz.jenda.pidifrky.logic.http

import java.net.SocketTimeoutException
import java.security.cert.CertificateException
import java.security.{KeyManagementException, KeyStoreException, NoSuchAlgorithmException}
import java.util.concurrent.TimeUnit

import com.google.common.io.ByteStreams
import com.squareup.okhttp.OkHttpClient
import cz.jenda.pidifrky.logic._
import cz.jenda.pidifrky.logic.exceptions._
import cz.jenda.pidifrky.proto.DeviceBackend.{DatabaseUpdateRequest, DatabaseUpdateResponse, DebugReportRequest}
import retrofit.RestAdapter.Builder
import retrofit.client.{OkClient, Response}
import retrofit.http.{Body, POST}
import retrofit.{Callback, RetrofitError}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object HttpRequester {

  /*execution context*/

  import cz.jenda.pidifrky.logic.Application._

  private lazy val client: OkClient = new OkClient({
    val okHttpClient = new OkHttpClient()
    okHttpClient.setConnectTimeout(1, TimeUnit.SECONDS)
    okHttpClient.setReadTimeout(10, TimeUnit.SECONDS)

    okHttpClient
  })

  protected lazy val httpsClient = new Builder()
    .setEndpoint(PidifrkyConstants.BASE_URL.replaceAll("http://", "https://"))
    .setClient(client)
    .setConverter(DeviceEnvelopeConverter)
    .build().create(classOf[PidifrkyService])

  protected lazy val httpClient = new Builder()
    .setEndpoint(PidifrkyConstants.BASE_URL.replaceAll("https://", "http://"))
    .setClient(client)
    .setConverter(DeviceEnvelopeConverter)
    .build().create(classOf[PidifrkyService])


  def debugReport(report: DebugReportRequest): Try[Unit] = exec(httpsClient.debugReport(report), httpClient.debugReport(report), PlainResponseDecoder) flatMap {
    case r if r.getStatus == 200 => Success(())
    case r => Failure(WrongHttpStatusException(r.getStatus))
  }

  def databaseUpdate(request: DatabaseUpdateRequest): Future[DatabaseUpdateResponse] =
    execAsync(httpsClient.databaseUpdate(request), httpClient.databaseUpdate(request), PlainResponseDecoder) { resp =>
      DatabaseUpdateResponse.parseFrom(resp.bytes)
    }

  private def exec[T](https: () => T, http: () => T, responseType: ResponseDecoder): Try[T] =
    Try {
      https()
    } transform(r => Success(r), t => Failure(t.getCause match {
      case e@(_: KeyStoreException | _: NoSuchAlgorithmException | _: CertificateException | _: KeyManagementException | _: javax.net.ssl.SSLException) =>
        DebugReporter.debug(e, "Error in SSL connection")
        SSLException(e)
      case e: RetrofitError if e.getMessage.startsWith("SSL") =>
        DebugReporter.debug(e, "Error in SSL connection")
        SSLException(e)
      case _ => GenericHttpException(t.getMessage, t)
    })) recover {
      case SSLException(t) =>
        DebugReporter.debugAndReport(t, "HTTPS connection failed, fallback to HTTP")

        http()
    }

  private def execAsync[T](https: Callback[Response] => Unit, http: Callback[Response] => Unit, responseType: ResponseDecoder)(toFinalResponse: HttpResponse => T): Future[T] = {
    val httpsPromise = Promise[Response]()

    https(new Callback[Response] {
      override def success(t: Response, response: Response): Unit =
        httpsPromise.complete(Success(t))

      override def failure(error: RetrofitError): Unit =
        httpsPromise.complete(Failure(error.getCause match {
          case e@(_: KeyStoreException | _: NoSuchAlgorithmException | _: CertificateException | _: KeyManagementException | _: javax.net.ssl.SSLException) =>
            DebugReporter.debug(e, "Error in SSL connection")
            SSLException(e)
          case e: SocketTimeoutException =>
            DebugReporter.debug(e, "Error in SSL connection")
            SSLException(e)
          case _ => GenericHttpException(error.getMessage, error.getCause)
        }))
    })

    httpsPromise.future recoverWith {
      case SSLException(t) =>
        DebugReporter.debugAndReport(t, "HTTPS connection failed, fallback to HTTP")
        val httpPromise = Promise[Response]()

        http(new Callback[Response] {
          override def success(t: Response, response: Response): Unit =
            httpPromise.complete(Success(t))

          override def failure(error: RetrofitError): Unit = {
            val response = error.getResponse
            val msg = "HTTP " + response.getStatus + " " + response.getReason + (Try(response.getBody).map(b => ByteStreams.toByteArray(b.in)) match {
              case Success(bytes) => " (" + new String(bytes) + ")"
              case Failure(_) => ""
            })

            httpPromise.complete(Failure(GenericHttpException(msg, error)))
          }
        })
        httpPromise.future
    } flatMap { r =>
      if (r.getStatus != 200) {
        val exception = WrongHttpStatusException(r.getStatus)
        DebugReporter.debugAndReport(exception)
        Future.failed(exception)
      }
      else {
        DebugReporter.debug(s"Downloaded ${Format.formatSize(r.getBody.length())}")

        Future.fromTry(Try {
          responseType.decode(r)
        }) map toFinalResponse recoverWith {
          case e: Exception => Future.failed(DecodeHttpException(r, e))
        }
      }
    }
  }
}

trait PidifrkyService {
  /* the /device prefix is already in BASE URL!!! */

  @POST("/debugReport")
  def debugReport(@Body report: DebugReportRequest)(): Response

  @POST("/updateDatabase")
  def databaseUpdate(@Body report: DatabaseUpdateRequest)(callback: Callback[Response]): Unit

}
