package cz.jenda.pidifrky.logic

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
sealed trait ActivityState

object ActivityState {
  case object Created extends ActivityState

  case object Started extends ActivityState

  case object Paused extends ActivityState

  case object Stopped extends ActivityState
}

