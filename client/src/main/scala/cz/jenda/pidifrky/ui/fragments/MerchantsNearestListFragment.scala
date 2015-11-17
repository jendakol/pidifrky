package cz.jenda.pidifrky.ui.fragments

import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.MerchantOrdering
import cz.jenda.pidifrky.data.dao.MerchantsDao
import cz.jenda.pidifrky.data.pojo.Merchant
import cz.jenda.pidifrky.logic.Application.executionContext
import cz.jenda.pidifrky.logic.FutureImplicits._
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.ui.api.{BasicActivity, EntityListTabFragment}
import cz.jenda.pidifrky.ui.lists.{BasicListAdapter, MerchantsListAdapter}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class MerchantsNearestListFragment(implicit ctx: BasicActivity) extends EntityListTabFragment[Merchant] {
  override val title: Option[String] = None

  override val icon: Option[Int] = Some(R.drawable.basket)

  protected lazy val listAdapter: BasicListAdapter[Merchant] = new MerchantsListAdapter(false)

  //TODO ordering
  protected implicit val ordering = MerchantOrdering.ByName

  override def onShow(): Unit = {
    LocationHandler.getCurrentLocation.foreach { loc =>
      MerchantsDao.getAll.foreachOnUIThread { merchs =>
        listAdapter.updateData(merchs)
      }
    }
  }
}
