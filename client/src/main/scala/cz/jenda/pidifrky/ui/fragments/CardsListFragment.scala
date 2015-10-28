package cz.jenda.pidifrky.ui.fragments

import cz.jenda.pidifrky.data.CardOrdering
import cz.jenda.pidifrky.data.dao.CardsDao
import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.logic.Application.executionContext
import cz.jenda.pidifrky.logic.FutureImplicits._
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.ui.api.{BasicActivity, EntityListTabFragment}
import cz.jenda.pidifrky.ui.lists.{BasicListAdapter, CardsListAdapter}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardsListFragment(implicit ctx: BasicActivity) extends EntityListTabFragment[Card] {
  override val title: String = "test Tab"
//TODO showLocation
  protected lazy val listAdapter: BasicListAdapter[Card] = new CardsListAdapter(showLocation = true)

  protected implicit val ordering = CardOrdering.ByName


  //  override protected def listAdapter: BasicListAdapter[Card] = {
  //
  //    val adapter = new CardsListAdapter(false)
  //
  //    LocationHandler.getCurrentLocation.foreach { loc =>
  //      CardsDao.getNearest(loc, 100000) foreachOnUIThread { cards =>
  //        adapter.updateData(cards)
  //      }
  //    }
  //
  //    //    CardsDao.getAll foreachOnUIThread { cards =>
  //    //      adapter.updateData(cards)
  //    //    }
  //
  //    adapter
  //  }


  override def onShow(): Unit = {
    LocationHandler.getCurrentLocation.foreach { loc =>
      CardsDao.getNearest(loc, 100000) foreachOnUIThread { cards =>
        listAdapter.updateData(cards)
      }
    }
  }
}
