package cz.jenda.pidifrky.ui

import android.os.Bundle
import cz.jenda.pidifrky.ui.api.{BasicTabActivity, TabFragment}
import cz.jenda.pidifrky.ui.fragments.{CardsAllListFragment, CardsNearestListFragment, MerchantsNearestListFragment}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class ListActivity extends BasicTabActivity {
  override protected lazy val tabs: Seq[TabFragment] = Seq(CardsAllListFragment(), CardsNearestListFragment(), MerchantsNearestListFragment())

  override protected lazy val preselectedTabIndex: Int = preselect

  private var preselect = 1

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    //this needs to be first!
    preselect = getIntent.getIntExtra(MapActivity.BundleKeys.ViewType, 1)

    super.onCreate(savedInstanceState)
  }
}
