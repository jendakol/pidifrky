package cz.jenda.pidifrky.ui

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.{CameraUpdateFactory, GoogleMap}
import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.logic.map.LocationHelper
import cz.jenda.pidifrky.ui.api.{BasicMapActivity, LineOptions}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class MapActivity extends BasicMapActivity {
  override def onMapReady(map: GoogleMap): Unit = {
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(49.8401903, 15.3693800), 8f))

    val card: Card = Card(1, 1, "Main", "main", Some(LocationHelper.toLocation(49.8401903, 15.3693800)), "", "")
    val card2: Card = Card(1, 1, "Main", "main", Some(LocationHelper.toLocation(49, 15.3693800)), "", "")

    //    addMarkers(card, card2)
    //    addLine(LineOptions(Color.RED, 5), card, card2)

    addDistanceLine(LineOptions(Color.RED, 5), card)
  }

  override protected def upButtonClicked(): Unit = {
    this.finish() //go back!
  }
}
