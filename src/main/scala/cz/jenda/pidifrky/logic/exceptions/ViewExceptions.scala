package cz.jenda.pidifrky.logic.exceptions

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class ViewException(message: String, cause: Throwable = null) extends PidifrkyException(message, cause)

case object CannotFindViewException extends ViewException(s"Requested view cannot be found")

case object AnotherTypeOfViewException extends ViewException(s"Requested view has another type then requested")
