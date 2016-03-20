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

  override protected val viewType = ViewType.AllCards

  override val iconResourceId: Option[Int] = Some(R.drawable.ic_view_list_white_36dp)

  override val actionBarMenuResourceId: Option[Int] = Some(R.menu.cards_list)

  //TODO ordering
  protected implicit val ordering = CardOrdering.ByName

  protected def updateCards(loc: Location): Unit = withCurrentActivity { implicit ctx =>
    CardsDao.getAll.foreachOnUIThread { cards =>
      listAdapter.updateData(cards)
    }
  }
}

object CardsAllListFragment {
  def apply()/*(implicit ctx: BasicActivity)*/: CardsAllListFragment = {
    val fr = new CardsAllListFragment
//    fr.ctx = ctx
    fr
  }
}
