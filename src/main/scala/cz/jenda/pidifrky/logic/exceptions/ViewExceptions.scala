package cz.jenda.pidifrky.logic.exceptions

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
case object CannotFindViewException extends PidifrkyException(s"Requested view cannot be found")

case object AnotherTypeOfViewException extends PidifrkyException(s"Requested view has another type then requested")
