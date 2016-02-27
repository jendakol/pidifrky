package cz.jenda.pidifrky.ui

import android.content.Intent
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.CardOrdering
import cz.jenda.pidifrky.data.dao.CardsDao
import cz.jenda.pidifrky.logic.FutureImplicits._
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.logic.map.MapMarker
import cz.jenda.pidifrky.ui.api.BasicMapActivity

import scala.util.Success

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class MapActivity extends BasicMapActivity {

  import CardOrdering.Implicits.ByName
  import MapActivity._

  override def onMapReady(map: GoogleMap, intent: Intent, clusterManager: ClusterManager[MapMarker]): Unit = {
    //    val card: Card = Card(1, 1, "Main", "main", Some(LocationHelper.toLocation(49.8401903, 15.3693800)), "", "")
    //    val card2: Card = Card(1, 1, "Main", "main", Some(LocationHelper.toLocation(49, 15.3693800)), "", "")

    //    addMarkers(card, card2)
    //    addLine(LineOptions(Color.RED, 5), card, card2)

    //    addDistanceLine(LineOptions(Color.RED, 5), card)

    Option(intent.getIntArrayExtra(BundleKeys.CardsIds)).foreach { ids =>
      withLoadToast(R.string.showing_cards) {
        CardsDao.get(ids.toSeq).andThenOnUIThread { case Success(cards) =>
          addMarkers(cards)
        }
      }
    }

  }

  override protected def upButtonClicked(): Unit = {
    this.finish() //go back!
  }

  override def onMapLongClick(latLng: LatLng): Unit = {
    LocationHandler.mockLocation(latLng)
  }
}

object MapActivity {

  object BundleKeys {
    private val prefix = getClass.getName + "_"

    final val CardsIds = prefix + "cardsIds"
  }

}
