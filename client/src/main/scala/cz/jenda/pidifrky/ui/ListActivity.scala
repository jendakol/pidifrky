package cz.jenda.pidifrky.ui

import android.os.Bundle
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.DebugReporter
import cz.jenda.pidifrky.ui.api.{BasicTabActivity, ExceptionHandler, NavigationDrawer, TabFragment}
import cz.jenda.pidifrky.ui.fragments.{CardsAllListFragment, CardsNearestListFragment, MerchantsNearestListFragment}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class ListActivity extends BasicTabActivity with NavigationDrawer with ExceptionHandler {
  override protected def tabLayoutId: Int = R.layout.activity_list

  override protected val hasParentActivity: Boolean = false

  override protected lazy val tabs: Seq[TabFragment] = Seq(CardsAllListFragment(), CardsNearestListFragment(), MerchantsNearestListFragment())

  override protected lazy val preselectedTabIndex: Int = preselect

  //it's mutable because it's set in onCreate
  private var preselect = 1

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    //this needs to be first!
    preselect = getIntent.getIntExtra(MapActivity.BundleKeys.ViewType, 1)

    super.onCreate(savedInstanceState)
  }

  override protected def onNavigationDrawerClick: PartialFunction[Int, Unit] = {
    case R.id.drawer_showSettings =>
      goTo(classOf[SettingsActivity])

    case _ =>
      DebugReporter.debug("not implemented")
  }
}
