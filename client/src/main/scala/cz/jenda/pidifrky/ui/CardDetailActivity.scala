package cz.jenda.pidifrky.ui

import java.util.Locale

import android.location.Location
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.{ImageView, TextView}
import com.malinskiy.superrecyclerview.SuperRecyclerView
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.MerchantOrdering
import cz.jenda.pidifrky.data.dao.{CardsDao, MerchantsDao}
import cz.jenda.pidifrky.data.pojo.{Card, Merchant}
import cz.jenda.pidifrky.logic.Utils
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.logic.map.LocationHelper
import cz.jenda.pidifrky.ui.api.{BasicActivity, OnItemClickListener, RecyclerViewItemClickListener, Toast}
import cz.jenda.pidifrky.ui.lists.MerchantsListAdapter

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardDetailActivity extends BasicActivity {

  import CardDetailActivity._

  override protected val actionBarMenu = Some(R.menu.card_detail)

  import MerchantOrdering.Implicits._

  private var card: Option[Card] = None
  private var merchants: Seq[Merchant] = Seq()

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.card_detail)

    val intent = getIntent

    val cardId = intent.getIntExtra(BundleKeys.CardId, 0)

    for {
      card <- CardsDao.get(cardId)
      imageUri <- card.getFullImageUri
      merchants <- MerchantsDao.get(card.merchantsIds)
    } yield Utils.runOnUiThread {
      val merchSeq = merchants.toSeq

      this.card = Option(card)
      this.merchants = merchSeq

      findView(R.id.name, classOf[TextView]).foreach(_.setText(card.name))
      findView(R.id.number, classOf[TextView]).foreach(_.setText(getText(R.string.number) + " " + card.number))
      findView(R.id.image, classOf[ImageView]).foreach(_.setImageURI(imageUri))

      for {
        loc <- card.location
        locationView <- findView(R.id.location, classOf[TextView])
      } yield {
        locationView.setText(LocationHelper.formatLocation(loc))

        LocationHandler
          .getCurrentLocation
          .map(_.distanceTo(loc) / 1000.0)
          .foreach(showCurrentDistance)
      }

      findView(R.id.list, classOf[SuperRecyclerView]).foreach { recyclerView =>
        recyclerView.setLayoutManager(new LinearLayoutManager(ctx))

        val adapter = new MerchantsListAdapter(merchSeq, true)
        recyclerView.setAdapter(adapter)

        RecyclerViewItemClickListener(recyclerView, new OnItemClickListener {
          override def onClick(view: View, position: Int): Unit = {
            adapter.getItem(position).foreach(onMerchantClick)
          }

          override def onLongClick(view: View, position: Int): Unit = {}
        })
      }
    }
  }


  override protected def onPostResume(): Unit = {
    super.onPostResume()
  }

  override protected def onStop(): Unit = {
    super.onStop()
  }


  override protected def onLocationChanged(currentLoc: Location): Unit = {
    super.onLocationChanged(currentLoc)

    for {
      card <- this.card
      loc <- card.location
      distanceView <- findView(R.id.distance, classOf[TextView])
    } yield {
      showCurrentDistance(currentLoc.distanceTo(loc) / 1000.0)
    }
  }

  protected def showCurrentDistance(dist: Double): Unit = Utils.runOnUiThread {
    val decimals = if (dist > 10) 1 else 2

    findView(R.id.distance, classOf[TextView]).foreach(_.setText(s"%.$decimals km".formatLocal(Locale.US, dist)))
  }

  protected def showBigImage(v: View): Unit = {
    Toast("Not supported", Toast.Short)
  }

  protected def showMap(v: View): Unit = {
    import MapActivity._

    card.foreach { card =>
      goWithParamsTo(classOf[MapActivity]) { intent =>
        card.location.foreach { loc =>
          intent.putExtra(BundleKeys.ShowLineTo, LocationHelper.toLatLng(loc))
        }

        intent.putExtra(BundleKeys.CardsIds, Array(card.id))
        intent.putExtra(BundleKeys.MerchantsIds, card.merchantsIds.toArray)
      }
    }
  }

  protected def changeListStatus(v: View): Unit = {
    Toast("Not supported", Toast.Short)
  }

  protected def onMerchantClick(m: Merchant): Unit = {
    Toast(s"Not supported - clicked on ${m.name}", Toast.Short)
  }
}

object CardDetailActivity {

  object BundleKeys {
    private val prefix = getClass.getName + "_"

    final val CardId = prefix + "cardId"
  }

}
