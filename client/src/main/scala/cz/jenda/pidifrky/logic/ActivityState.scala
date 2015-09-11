package cz.jenda.pidifrky.logic

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
sealed trait ActivityState

case object CreatedState extends ActivityState

case object StartedState extends ActivityState

case object PausedState extends ActivityState

case object StoppedState extends ActivityState