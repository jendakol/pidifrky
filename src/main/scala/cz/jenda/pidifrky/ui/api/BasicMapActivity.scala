package cz.jenda.pidifrky.ui.api

import android.os.Bundle
import com.google.android.gms.maps.model.{BitmapDescriptorFactory, MarkerOptions, PolylineOptions}
import com.google.android.gms.maps.{GoogleMap, OnMapReadyCallback, SupportMapFragment}
import com.google.maps.android.ui.IconGenerator
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.IMapPoint
import cz.jenda.pidifrky.data.pojo.Entity
import cz.jenda.pidifrky.logic.map.{LocationHelper, MapType}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicMapActivity extends BasicActivity with OnMapReadyCallback {
  private val iconGenerator = new IconGenerator(this)

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

  def clearMap(): Unit = map.foreach(_.clear)

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

  def addLine(options: LineOptions, points: IMapPoint*): Unit = map foreach { map =>
    import scala.collection.JavaConverters._
    val seq = points.map(_.gps.map(LocationHelper.toLatLng)).filter(_.isDefined).map(_.get)
    val polyline = new PolylineOptions().addAll(seq.asJava).width(options.width).color(options.color)
    map.addPolyline(polyline)
  }

  def addDistanceLine(options: LineOptions, entity: Entity): Unit = for {
    map <- getMap
    location <- entity.gps
    marker <- entity.toMarker
    currentLocation <- Some(LocationHelper.toLocation(49, 15.3693800))
  } yield {
      val polyline = new PolylineOptions()
        .add(LocationHelper.toLatLng(location), LocationHelper.toLatLng(currentLocation))
        .width(options.width)
        .color(options.color)

      map.addPolyline(polyline)

      val icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("%.2f km".format(location.distanceTo(currentLocation) / 1000.0)))

      map.addMarker(new MarkerOptions().position(LocationHelper.getCenter(location, currentLocation)).icon(icon))

      map.addMarker(marker.getMarker)
    }
}

case class LineOptions(color: Int, width: Int)