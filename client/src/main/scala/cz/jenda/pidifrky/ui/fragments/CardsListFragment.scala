package cz.jenda.pidifrky.ui.fragments

import android.content.Intent
import android.location.Location
import android.view.Menu
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.logic.Application
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.ui.MapActivity.ViewType
import cz.jenda.pidifrky.ui.api.EntityListTabFragment
import cz.jenda.pidifrky.ui.{CardDetailActivity, MapActivity}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait CardsListFragment extends EntityListTabFragment[Card] {
  override val title: Option[String] = None

  override val actionBarMenuResourceId: Option[Int] = Some(R.menu.cards_list)

  protected def viewType: ViewType

  override def onShow(): Unit = {
    if (!preload) LocationHandler.getCurrentLocation.foreach(updateCards)
    LocationHandler.setListener(updateCards)
  }

  protected def updateCards(loc: Location): Unit

  override def onHide(): Unit = {
    LocationHandler.unSetListener
  }

  override def onMenuInflate(menu: Menu): Unit = {
    Option(menu.findItem(R.id.menu_cards_gpsOn)).foreach(_.setVisible(LocationHandler.isMockingLocation))
  }

  override def onMenuAction: PartialFunction[Int, Unit] = {
    case R.id.menu_cards_gpsOn =>
      LocationHandler.disableMocking

    case R.id.menu_cards_showMap =>
      ctx.goWithParamsTo(classOf[MapActivity]) { intent =>
        intent.putExtra(MapActivity.BundleKeys.ViewType, viewType.id)
      }
  }

  override def onClick(card: Card): Unit = {
    Application.withCurrentContext { implicit ctx =>
      val intent = new Intent(ctx, classOf[CardDetailActivity])
      intent.putExtra(CardDetailActivity.BundleKeys.CardId, card.id)

      ctx.startActivity(intent)
    }
  }

  override def onLongClick(card: Card): Unit = {}
}
