package cz.jenda.pidifrky.ui

import android.graphics.Color
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.{CameraUpdateFactory, GoogleMap}
import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.logic.PidifrkyConstants
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.logic.map.LocationHelper
import cz.jenda.pidifrky.ui.api.{BasicMapActivity, LineOptions}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class MapActivity extends BasicMapActivity with OnMapLongClickListener {
  override def onMapReady(map: GoogleMap): Unit = {
    map.moveCamera(CameraUpdateFactory.newLatLngZoom(PidifrkyConstants.MAP_CENTER, 8f))

    val card: Card = Card(1, 1, "Main", "main", Some(LocationHelper.toLocation(49.8401903, 15.3693800)), "", "")
    val card2: Card = Card(1, 1, "Main", "main", Some(LocationHelper.toLocation(49, 15.3693800)), "", "")

    //    addMarkers(card, card2)
    //    addLine(LineOptions(Color.RED, 5), card, card2)

    addDistanceLine(LineOptions(Color.RED, 5), card)

    map.setOnMapLongClickListener(this)
  }

  override protected def upButtonClicked(): Unit = {
    this.finish() //go back!
  }

  override def onMapLongClick(latLng: LatLng): Unit = {
    LocationHandler.mockLocation(latLng)
  }
}
