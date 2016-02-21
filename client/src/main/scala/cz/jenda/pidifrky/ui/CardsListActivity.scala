package cz.jenda.pidifrky.ui

import cz.jenda.pidifrky.ui.api.{BasicTabActivity, TabFragment}
import cz.jenda.pidifrky.ui.fragments.{CardsAllListFragment, CardsNearestListFragment, MerchantsNearestListFragment}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardsListActivity extends BasicTabActivity {
  override protected lazy val tabs: Seq[TabFragment] = Seq(CardsAllListFragment(), CardsNearestListFragment(), MerchantsNearestListFragment())

  override protected lazy val preselectedTabIndex: Int = 1
}
