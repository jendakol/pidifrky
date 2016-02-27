package cz.jenda.pidifrky.ui.fragments

import android.view.Menu
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.MerchantOrdering
import cz.jenda.pidifrky.data.dao.MerchantsDao
import cz.jenda.pidifrky.data.pojo.Merchant
import cz.jenda.pidifrky.logic.Application.executionContext
import cz.jenda.pidifrky.logic.FutureImplicits._
import cz.jenda.pidifrky.logic.PidifrkySettings
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.ui.api.{BasicActivity, EntityListTabFragment}
import cz.jenda.pidifrky.ui.lists.{BasicListAdapter, MerchantsListAdapter}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class MerchantsNearestListFragment extends EntityListTabFragment[Merchant] {

  override protected val preload = true

  override val title: Option[String] = None

  override val iconResourceId: Option[Int] = Some(R.drawable.basket)

  protected lazy val listAdapter: BasicListAdapter[Merchant] = new MerchantsListAdapter(false)

  override val actionBarMenuResourceId: Option[Int] = None


  //TODO ordering
  protected implicit val ordering = MerchantOrdering.ByName

  override def onShow(): Unit = {
    LocationHandler.getCurrentLocation.foreach { loc =>
      MerchantsDao.getNearest(loc, PidifrkySettings.closestDistance).foreachOnUIThread { merchs =>
        listAdapter.updateData(merchs)
      }
    }
  }

  override def onMenuInflate(menu: Menu): Unit = {}

  override def onMenuAction: PartialFunction[Int, Unit] = PartialFunction.empty
}

object MerchantsNearestListFragment {
  def apply()(implicit ctx: BasicActivity): MerchantsNearestListFragment = {
    val fr = new MerchantsNearestListFragment
    fr.ctx = ctx
    fr
  }
}
