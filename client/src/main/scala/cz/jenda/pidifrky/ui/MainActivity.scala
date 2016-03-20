package cz.jenda.pidifrky.ui

import android.os.Bundle
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.{Card, Entity, Merchant}
import cz.jenda.pidifrky.ui.api._

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class MainActivity extends BasicActivityWithDrawer with ExceptionHandler {

  override protected val hasParentActivity: Boolean = false

  override protected def layoutResourceId = Some(R.layout.main)

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
  }

  def onEntityClick(entity: Entity): Unit = entity match {
    case c: Card => showCardDetail(c)
    case m: Merchant => showMerchantDetail(m)
  }

  def onEntityLongClick(entity: Entity): Unit = {
    Toast(s"long click on ${entity.name} - not supported", Toast.Short)
  }

  def showCardDetail(card: Card): Unit = {
    goWithParamsTo(classOf[CardDetailActivity]) { intent =>
      intent.putExtra(CardDetailActivity.BundleKeys.CardId, card.id)
    }
  }

  def showMerchantDetail(merchant: Merchant): Unit = {
    Toast(s"Detail of ${merchant.name} - not supported", Toast.Short)
  }

}
