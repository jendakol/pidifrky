package cz.jenda.pidifrky.ui

import cz.jenda.pidifrky.ui.api.{BasicTabActivity, TabFragment}
import cz.jenda.pidifrky.ui.fragments.{CardsListFragment, TestTabFragment2}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardsListActivity extends BasicTabActivity {
  override protected def tabs: List[TabFragment] = List(new TestTabFragment2, new CardsListFragment)

}
