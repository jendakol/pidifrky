package cz.jenda.pidifrky.logic.exceptions

import cz.jenda.pidifrky.logic.Format
import retrofit.client.Response


/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class HttpException(message: String, cause: Throwable = null) extends PidifrkyException(message, cause) {
  override def toString: String = getClass.getSimpleName + s"{'$message', caused by ${if (cause != null) Format(cause) else "<nothing>"}}"
}

case class GenericHttpException(message: String, cause: Throwable = null) extends HttpException(message, cause)

case class SSLException(cause: Throwable) extends HttpException(s"SSL connection has failed", cause)

case class WrongHttpStatusException(httpStatus: Int) extends HttpException(s"Invalid HTTP status, was $httpStatus")

case class DecodeHttpException(r: Response, cause: Throwable = null) extends HttpException("Error while decoding the HTTP response", cause)
