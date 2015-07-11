package cz.jenda.pidifrky.logic

import cz.jenda.pidifrky.ui.api.{BasicActivity, Orientation, PortraitOrientation}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object Application {
  var currentActivity: Option[BasicActivity] = None
  var currentOrientation: Orientation = PortraitOrientation
}
