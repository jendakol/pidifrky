package cz.jenda.pidifrky.logic.exceptions

/**
 * @author Jenda Kolena, kolena@avast.com
 */
case object OfflineException extends PidifrkyException("Device is offline")

case object NoStorageException extends PidifrkyException("No storage is available")
