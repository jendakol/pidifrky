package cz.jenda.pidifrky.logic.exceptions

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
case object AnotherTypeOfViewException extends PidifrkyException(s"Requested view has another type then requested")
