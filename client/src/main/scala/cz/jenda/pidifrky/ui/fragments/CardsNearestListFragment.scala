package cz.jenda.pidifrky.ui.fragments

import android.location.Location
import android.view.Menu
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.CardOrdering
import cz.jenda.pidifrky.data.dao.CardsDao
import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.logic.Application.executionContext
import cz.jenda.pidifrky.logic.FutureImplicits._
import cz.jenda.pidifrky.logic.PidifrkySettings
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.ui.MapActivity
import cz.jenda.pidifrky.ui.MapActivity.ViewType
import cz.jenda.pidifrky.ui.api.{BasicActivity, EntityListTabFragment}
import cz.jenda.pidifrky.ui.lists.{BasicListAdapter, CardsListAdapter}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardsNearestListFragment extends EntityListTabFragment[Card] {

  override protected val preload = true

  override val title: Option[String] = None

  override val iconResourceId: Option[Int] = Some(R.drawable.ic_gps_fixed_white_36dp)

  override val actionBarMenuResourceId: Option[Int] = Some(R.menu.cards_list)

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
    //TODO ordering
    implicit val ord = CardOrdering.ByDistance(loc)

    CardsDao.getNearest(loc, PidifrkySettings.closestDistance).foreachOnUIThread { cards =>
      listAdapter.updateData(cards)
    }
  }

  override def onHide(): Unit = {
    LocationHandler.removeListener
  }

  override def onMenuInflate(menu: Menu): Unit = {
    Option(menu.findItem(R.id.menu_cards_gpsOn)).foreach(_.setVisible(LocationHandler.isMockingLocation))
  }

  override def onMenuAction: PartialFunction[Int, Unit] = {
    case R.id.menu_cards_gpsOn =>
      LocationHandler.disableMocking

    case R.id.menu_cards_showMap =>
      ctx.goWithParamsTo(classOf[MapActivity]) { intent =>
        intent.putExtra(MapActivity.BundleKeys.ViewType, ViewType.NearestCards.id)
      }
  }
}

object CardsNearestListFragment {
  def apply()(implicit ctx: BasicActivity): CardsNearestListFragment = {
    val fr = new CardsNearestListFragment
    fr.ctx = ctx
    fr
  }
}
