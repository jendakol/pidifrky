package cz.jenda.pidifrky.ui.fragments

import android.location.Location
import android.view.Menu
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.MerchantOrdering
import cz.jenda.pidifrky.data.dao.MerchantsDao
import cz.jenda.pidifrky.data.pojo.Merchant
import cz.jenda.pidifrky.logic.Application.executionContext
import cz.jenda.pidifrky.logic.FutureImplicits._
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.logic.{DebugReporter, PidifrkySettings}
import cz.jenda.pidifrky.ui.MapActivity
import cz.jenda.pidifrky.ui.MapActivity.ViewType
import cz.jenda.pidifrky.ui.api.EntityListTabFragment
import cz.jenda.pidifrky.ui.lists.{BasicListAdapter, MerchantsListAdapter}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class MerchantsNearestListFragment extends EntityListTabFragment[Merchant] {

  override protected val preload = true

  override val title: Option[String] = None

  override val iconResourceId: Option[Int] = Some(R.drawable.basket)

  //TODO ordering
  protected implicit val ordering = MerchantOrdering.ByName

  protected lazy val listAdapter: BasicListAdapter[Merchant] = withCurrentActivity { implicit ctx =>
    DebugReporter.debug("Initializing list adapter for merchants")

    //TODO show location
    val adapter = new MerchantsListAdapter(showLocation = true)
    if (preload) {
      LocationHandler.getCurrentLocation.foreach(updateMerchants)
    }
    adapter
  }

  override val actionBarMenuResourceId: Option[Int] = Some(R.menu.merchants_list)

  override def onShow(): Unit = withCurrentActivity { implicit ctx =>
    LocationHandler.getCurrentLocation.foreach(updateMerchants)
  }

  protected def updateMerchants(loc: Location): Unit = withCurrentActivityIfPossible { implicit ctx =>
    MerchantsDao.getNearest(loc, PidifrkySettings.closestDistance).foreachOnUIThread { merchs =>
      listAdapter.updateData(merchs)
    }
  }

  override def onMenuInflate(menu: Menu): Unit = {
    Option(menu.findItem(R.id.menu_merchants_gpsOn)).foreach(_.setVisible(LocationHandler.isMockingLocation))
  }

  override def onMenuAction: PartialFunction[Int, Unit] = {
    case R.id.menu_merchants_gpsOn =>
      withCurrentActivity { implicit ctx =>
        LocationHandler.disableMocking
      }

    case R.id.menu_merchants_showMap =>
      withCurrentActivity { implicit ctx =>
        ctx.goWithParamsTo(classOf[MapActivity]) { intent =>
          intent.putExtra(MapActivity.BundleKeys.ViewType, ViewType.NearestMerchants.id)
        }
      }
  }

  override def onClick(entity: Merchant): Unit = {}

  override def onLongClick(entity: Merchant): Unit = {}
}

object MerchantsNearestListFragment {
  def apply() /*(implicit ctx: BasicActivity)*/ : MerchantsNearestListFragment = {
    val fr = new MerchantsNearestListFragment
    //    fr.ctx = ctx
    fr
  }
}
