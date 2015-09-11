package cz.jenda.pidifrky.data

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
sealed trait CardState {

}

case object CardNormalState extends CardState

case object CardOwnedState extends CardState

case object CardWantedState extends CardState