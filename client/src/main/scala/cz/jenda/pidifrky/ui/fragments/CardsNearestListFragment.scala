package cz.jenda.pidifrky.ui.fragments

import android.location.Location
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.CardOrdering
import cz.jenda.pidifrky.data.dao.CardsDao
import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.logic.Application.executionContext
import cz.jenda.pidifrky.logic.FutureImplicits._
import cz.jenda.pidifrky.logic.PidifrkySettings
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.ui.MapActivity.ViewType
import cz.jenda.pidifrky.ui.api.BasicActivity
import cz.jenda.pidifrky.ui.lists.{BasicListAdapter, CardsListAdapter}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardsNearestListFragment extends CardsListFragment {

  override protected val preload = true

  override protected val viewType = ViewType.NearestCards

  override val iconResourceId: Option[Int] = Some(R.drawable.ic_gps_fixed_white_36dp)

  protected def updateCards(loc: Location): Unit = withCurrentActivityIfPossible { implicit ctx =>
    //TODO ordering
    implicit val ord = CardOrdering.ByDistance(loc)

    CardsDao.getNearest(loc, PidifrkySettings.closestDistance).foreachOnUIThread { cards =>
      listAdapter.updateData(cards)
    }
  }
}

object CardsNearestListFragment {
  def apply()/*(implicit ctx: BasicActivity)*/: CardsNearestListFragment = {
    val fr = new CardsNearestListFragment
    //    fr.ctx = ctx
    fr
  }
}
