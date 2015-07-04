package cz.jenda.pidifrky.ui

import android.os.Bundle
import cz.jenda.pidifrky.ui.api.{BasicTabActivity, TabFragment}
import cz.jenda.pidifrky.ui.fragments.{TestTabFragment, TestTabFragment2}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardsListActivity extends BasicTabActivity {
  override protected def tabs: List[TabFragment] = List(new TestTabFragment2, new TestTabFragment)

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
  }

}
