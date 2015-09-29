package cz.jenda.pidifrky.ui

import cz.jenda.pidifrky.ui.api.{BasicTabActivity, TabFragment}
import cz.jenda.pidifrky.ui.fragments.{CardsListFragment, MerchantsListFragment}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardsListActivity extends BasicTabActivity {
  override protected lazy val tabs: List[TabFragment] = List(new CardsListFragment, new MerchantsListFragment)
}
