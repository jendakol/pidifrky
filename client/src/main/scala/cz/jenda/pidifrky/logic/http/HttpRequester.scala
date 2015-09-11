package cz.jenda.pidifrky.logic.http

import java.security.cert.CertificateException
import java.security.{KeyManagementException, KeyStoreException, NoSuchAlgorithmException}

import android.content.pm.PackageManager
import com.squareup.okhttp.OkHttpClient
import cz.jenda.pidifrky.logic._
import cz.jenda.pidifrky.logic.exceptions._
import retrofit.RequestInterceptor.RequestFacade
import retrofit.RestAdapter.Builder
import retrofit.client.{OkClient, Response}
import retrofit.http.GET
import retrofit.{Callback, RequestInterceptor, RetrofitError}

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success, Try}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object HttpRequester {

  /*execution context*/

  import cz.jenda.pidifrky.logic.Application._


  private lazy val requestInterceptor: RequestInterceptor = new RequestInterceptor {
    override def intercept(request: RequestFacade): Unit = {
      request.addHeader("secret", "a4e4924865e9d745d4ad8e") //since the source is public, it loses much of its sense, but it can still be used for filtering of some bullshit requests...
      request.addHeader("uuid", PidifrkySettings.UUID)
      request.addHeader("debug", Utils.isDebug.toString)

      try Application.currentActivity.foreach { ctx =>
        val packageInfo = ctx.getPackageManager.getPackageInfo(ctx.getPackageName, 0)
        request.addHeader("appVersion", packageInfo.versionName + "/" + packageInfo.versionCode)
      }
      catch {
        case e: PackageManager.NameNotFoundException =>
          //ignore...
          DebugReporter.debugAndReport(e)
      }
    }
  }
  private lazy val client: OkClient = new OkClient(new OkHttpClient())

  protected lazy val httpsClient = new Builder()
    .setEndpoint(PidifrkyConstants.BASE_URL.replaceAll("http://", "https://"))
    .setClient(client)
    .setRequestInterceptor(requestInterceptor)
    .build().create(classOf[PidifrkyService])

  protected lazy val httpClient = new Builder()
    .setEndpoint(PidifrkyConstants.BASE_URL.replaceAll("https://", "http://"))
    .setClient(client)
    .build().create(classOf[PidifrkyService])


  def ping: Future[String] = exec(httpsClient.ping, httpClient.ping, PlainResponseDecoder) { r =>
    r.status.toString
  }

  private def exec[T](https: Callback[Response] => Unit, http: Callback[Response] => Unit, responseType: ResponseDecoder)(toFinalResponse: HttpResponse => T): Future[T] = {
    val httpsPromise = Promise[Response]()

    https(new Callback[Response] {
      override def success(t: Response, response: Response): Unit =
        httpsPromise.complete(Success(t))

      override def failure(error: RetrofitError): Unit =
        httpsPromise.complete(Failure(error.getCause match {
          case e@(_: KeyStoreException | _: NoSuchAlgorithmException | _: CertificateException | _: KeyManagementException | _: javax.net.ssl.SSLException) =>
            DebugReporter.debug(e, "Error in SSL connection")
            SSLException(e)
          case _ => HttpException(error)
        }))
    })

    httpsPromise.future recoverWith {
      case SSLException(t) =>
        DebugReporter.debugAndReport(t, "HTTPS connection failed, fallback to HTTP")
        val httpPromise = Promise[Response]()

        http(new Callback[Response] {
          override def success(t: Response, response: Response): Unit =
            httpPromise.complete(Success(t))

          override def failure(error: RetrofitError): Unit =
            httpPromise.complete(Failure(HttpException(error)))
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
  @GET("/")
  def ping(callback: Callback[Response])

}
