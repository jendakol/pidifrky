package exceptions

import com.google.protobuf.Message

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */

abstract class ClientException(message: String, cause: Throwable = null) extends PidifrkyException(message, cause)

abstract class ServerException(message: String, cause: Throwable = null) extends PidifrkyException(message, cause)

case class ContentCannotBeParsedException(expectedClass: Class[_ <: Message]) extends ClientException("Content cannot be parsed as " + expectedClass.getSimpleName)

case class InvalidPayloadException(service: String, expectedContent: String) extends ServerException(s"Payload received from $service cannot be parsed as $expectedContent")