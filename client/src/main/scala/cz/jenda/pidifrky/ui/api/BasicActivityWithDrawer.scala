package cz.jenda.pidifrky.ui.api

import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.DebugReporter
import cz.jenda.pidifrky.ui.SettingsActivity

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicActivityWithDrawer extends BasicActivity with NavigationDrawer {
  override protected def onNavigationDrawerClick: PartialFunction[Int, Unit] = {
    case R.id.drawer_showSettings =>
      goTo(classOf[SettingsActivity])

    case _ =>
      DebugReporter.debug("not implemented")
  }
}
