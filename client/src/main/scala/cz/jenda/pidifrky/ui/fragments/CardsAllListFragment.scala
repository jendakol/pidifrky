package cz.jenda.pidifrky.ui.fragments

import android.location.Location
import android.view.Menu
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.CardOrdering
import cz.jenda.pidifrky.data.dao.CardsDao
import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.logic.Application.executionContext
import cz.jenda.pidifrky.logic.FutureImplicits._
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.ui.MapActivity
import cz.jenda.pidifrky.ui.MapActivity.ViewType
import cz.jenda.pidifrky.ui.api.{BasicActivity, EntityListTabFragment}
import cz.jenda.pidifrky.ui.lists.{BasicListAdapter, CardsListAdapter}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardsAllListFragment extends CardsListFragment {

  override protected val preload = false

  override val iconResourceId: Option[Int] = Some(R.drawable.ic_view_list_white_36dp)

  override val actionBarMenuResourceId: Option[Int] = Some(R.menu.cards_list)

  //TODO ordering
  protected implicit val ordering = CardOrdering.ByName

  override protected lazy val listAdapter: BasicListAdapter[Card] = {

    //TODO show location
    val adapter = new CardsListAdapter(showLocation = true)
    if (preload) {
      LocationHandler.getCurrentLocation.foreach(updateCards)
    }
    adapter
  }

  protected def updateCards(loc: Location): Unit = {
    CardsDao.getAll.foreachOnUIThread { cards =>
      listAdapter.updateData(cards)
    }
  }

}

object CardsAllListFragment {
  def apply()(implicit ctx: BasicActivity): CardsAllListFragment = {
    val fr = new CardsAllListFragment
    fr.ctx = ctx
    fr
  }
}
