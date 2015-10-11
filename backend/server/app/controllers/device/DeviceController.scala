package controllers.device

import com.google.protobuf.{Message, Parser, TextFormat}
import cz.jenda.pidifrky.proto.DeviceBackend.Envelope
import cz.jenda.pidifrky.proto.DeviceBackend.Envelope.DeviceInfo
import exceptions.{ClientException, ContentCannotBeParsedException, ServerException}
import play.api.http.Writeable
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.mvc._
import play.mvc.Controller
import utils.Logging

import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

/**
 * @author Jenda Kolena, kolena@avast.com
 */
trait DeviceController extends Controller with Results with Logging {

  //noinspection AccessorLikeMethodIsUnit
  protected def toUnit(a: Any) = ()

  def deviceRequest[C <: Message](p: Parser[C])(implicit r: Request[AnyContent]): Try[DeviceRequest[C]] = deviceEnvelope map { env =>
    DeviceRequest(env.getUuid, env.getAppVersion, env.getDeviceInfo, if (env.hasDebug) Some(env.getDebug) else None, p.parseFrom(env.getData))
  }

  def deviceEnvelope(implicit r: Request[AnyContent]): Try[Envelope] =
    r.body.asRaw.flatMap(toDeviceEnvelope) match {
      case Some(e) => Success(e)
      case None => Failure(ContentCannotBeParsedException(classOf[Envelope]))
    }

  protected def toDeviceEnvelope(b: RawBuffer): Option[Envelope] = b.asBytes().map(Envelope.parseFrom)
}

object DeviceControllerImplicits extends Results {
  implicit val Logger = play.api.Logger(getClass)

  implicit val gpbWriteable: Writeable[Message] = new Writeable(e => e.toByteArray, Some("application/x-protobuf"))

  implicit class TryToResponse[C <: Message](val t: Try[C]) extends AnyVal{
    def toResponse: Result = t match {
      case Success(r) =>
        Logger.debug("Returning OK")
        Ok(r)
      case Failure(e: ClientException) =>
        Logger.debug("Returning BadRequest with message " + e.getMessage)
        BadRequest(e.getMessage)
      case Failure(e: ServerException) =>
        Logger.debug("Returning InternalServerError with message " + e.getMessage)
        InternalServerError(e.getMessage)
      case Failure(e) =>
        Logger.debug("Returning InternalServerError with message " + e.getMessage)
        InternalServerError(e.getMessage)
    }
  }

  implicit class TryToResponseUnit(t: Try[Unit]) {
    def toResponse: Result = t match {
      case Success(_) =>
        Logger.debug("Returning OK")
        Ok("")
      case Failure(e: ClientException) =>
        Logger.debug("Returning BadRequest with message " + e.getMessage)
        BadRequest(e.getMessage)
      case Failure(e: ServerException) =>
        Logger.debug("Returning InternalServerError with message " + e.getMessage)
        InternalServerError(e.getMessage)
      case Failure(e) =>
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