package cz.jenda.pidifrky.ui.api

import android.os.Bundle
import com.google.android.gms.maps.{GoogleMap, OnMapReadyCallback, SupportMapFragment}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.Entity
import cz.jenda.pidifrky.logic.map.MapType

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicMapActivity extends BasicActivity with OnMapReadyCallback {

  private var map: Option[GoogleMap] = None

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.map)
    Option(getSupportFragmentManager.findFragmentById(R.id.map)) match {
      case Some(f: SupportMapFragment) => f.getMapAsync(new OnMapReadyCallback {
        override def onMapReady(googleMap: GoogleMap): Unit = {
          map = Some(googleMap)

          googleMap.setIndoorEnabled(false)

          BasicMapActivity.this.onMapReady(googleMap)
        }
      })
      case Some(f) => //TODO: warning
      case None => //TODO: warning
    }
  }

  def getMap: Option[GoogleMap] = map

  def setMapType(mapType: MapType): Unit = map.foreach(_.setMapType(mapType.id))

  def addMarkers(entities: Entity*): Unit = {
    map.foreach { map =>
      entities.map(_.toMarker) foreach {
        case Some(marker) =>
          map.addMarker(marker.getMarker)
        case _ =>
      }
    }
  }
}
