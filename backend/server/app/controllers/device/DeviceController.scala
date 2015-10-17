package controllers.device

import com.google.protobuf.{Message, Parser, TextFormat}
import cz.jenda.pidifrky.proto.DeviceBackend.Envelope
import cz.jenda.pidifrky.proto.DeviceBackend.Envelope.DeviceInfo
import exceptions.{ClientException, ContentCannotBeParsedException, ServerException}
import play.api.http.Writeable
import play.api.mvc._
import play.mvc.Controller
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.control.NonFatal

/**
  * @author Jenda Kolena, kolena@avast.com
  */
trait DeviceController extends Controller with Results with Logging {

  import logic.AppModule._

  protected implicit val ec: ExecutionContext = callbackExecutor

  //noinspection AccessorLikeMethodIsUnit
  protected def toUnit(a: Any) = ()

  def deviceRequest[C <: Message](p: Parser[C])(implicit r: Request[AnyContent]): Future[DeviceRequest[C]] = deviceEnvelope map { env =>
    DeviceRequest(env.getUuid, env.getAppVersion, env.getDeviceInfo, if (env.hasDebug) Some(env.getDebug) else None, p.parseFrom(env.getData))
  }

  def deviceEnvelope(implicit r: Request[AnyContent]): Future[Envelope] = r.body.asRaw.flatMap(_.asBytes()) match {
    case Some(body) => Future {
      Envelope.parseFrom(body)
    }(blockingExecutor)
    case None => Future.failed(ContentCannotBeParsedException(classOf[Envelope]))
  }
}

object DeviceControllerImplicits extends Results with Logging {

  import logic.AppModule._

  implicit val gpbWriteable: Writeable[Message] = new Writeable(e => e.toByteArray, Some("application/x-protobuf"))

  implicit class FutureToResponse[C <: Message](val t: Future[C]) extends AnyVal {
    def toResponse: Future[Result] =
      t.map { r =>
        Logger.debug("Returning OK")
        Ok(r)
      } recover {
        case e: ClientException =>
          Logger.debug("Returning BadRequest with message " + e.getMessage, e)
          BadRequest(e.getMessage)
        case e: ServerException =>
          Logger.debug("Returning InternalServerError with message " + e.getMessage, e)
          InternalServerError(e.getMessage)
        case NonFatal(e) =>
          Logger.debug("Returning InternalServerError with message " + e.getMessage, e)
          InternalServerError(e.getMessage)
      }
  }

  implicit class FutureToResponseUnit(t: Future[Unit]) {
    def toResponse: Future[Result] =
      t.map { _ =>
        Logger.debug("Returning OK")
        Ok("")
      } recover {
        case (e: ClientException) =>
          Logger.debug("Returning BadRequest with message " + e.getMessage)
          BadRequest(e.getMessage)
        case (e: ServerException) =>
          Logger.debug("Returning InternalServerError with message " + e.getMessage)
          InternalServerError(e.getMessage)
        case NonFatal(e) =>
          Logger.debug("Returning InternalServerError with message " + e.getMessage)
          InternalServerError(e.getMessage)
      }
  }

}

case class DeviceRequest[C <: Message](uuid: String, appVersion: String, deviceInfo: DeviceInfo, debug: Option[Boolean], data: C) {
  def export: String = {
    s"uuid=$uuid\nappVersion=$appVersion\ndebug=${debug.getOrElse("-")}\n" +
      TextFormat.printToString(deviceInfo) + "\n" +
      TextFormat.printToString(data)
  }
}

case class ExecutionContexts(callback: ExecutionContext, blocking: ExecutionContext) {
  implicit val ec: ExecutionContext = callback
}
