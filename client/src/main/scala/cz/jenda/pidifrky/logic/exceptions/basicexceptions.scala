package cz.jenda.pidifrky.logic.exceptions

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
case object OfflineException extends PidifrkyException("Device is offline")

case object NoStorageException extends PidifrkyException("No storage is available")

case class ResourceNotFoundException(name: String) extends PidifrkyException(s"Resource '$name' wasn't found")
