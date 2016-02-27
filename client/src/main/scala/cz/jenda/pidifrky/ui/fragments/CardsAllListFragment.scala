package cz.jenda.pidifrky.ui.fragments

import android.location.Location
import cz.jenda.pidifrky.R
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
class CardsAllListFragment(preload: Boolean = false)(implicit ctx: BasicActivity) extends EntityListTabFragment[Card] {

  override val title: Option[String] = None

  override val icon: Option[Int] = Some(R.drawable.ic_reorder_white_36dp)

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

  override def onShow(): Unit = {
    if (!preload) LocationHandler.getCurrentLocation.foreach(updateCards)
    LocationHandler.addListener(updateCards)
  }

  protected def updateCards(loc: Location): Unit = {
    CardsDao.getAll.foreachOnUIThread { cards =>
      listAdapter.updateData(cards)
    }
  }

  override def onHide(): Unit = {
    LocationHandler.removeListener
  }
}
