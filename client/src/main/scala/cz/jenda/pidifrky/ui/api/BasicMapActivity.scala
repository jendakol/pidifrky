package cz.jenda.pidifrky.ui.api

import android.content.DialogInterface.OnDismissListener
import android.content.{Context, DialogInterface, Intent}
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import com.google.android.gms.common.{ConnectionResult, GoogleApiAvailability}
import com.google.android.gms.maps.GoogleMap.{CancelableCallback, OnCameraChangeListener, OnInfoWindowClickListener, OnMapLongClickListener}
import com.google.android.gms.maps.model._
import com.google.android.gms.maps.{CameraUpdateFactory, GoogleMap, OnMapReadyCallback, SupportMapFragment}
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.IMapPoint
import cz.jenda.pidifrky.data.pojo.Entity
import cz.jenda.pidifrky.logic.location.{ActivityLocationResolver, LocationHandler}
import cz.jenda.pidifrky.logic.map._
import cz.jenda.pidifrky.logic.{DebugReporter, PidifrkyConstants, PidifrkySettings, Utils}
import cz.jenda.pidifrky.ui.dialogs.InfoDialog

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicMapActivity extends BasicActivity with OnMapLongClickListener with OnCameraChangeListener {
  private lazy val iconGenerator = new IconGenerator(this)

  private var map: Option[GoogleMap] = None
  private var clusterManager: Option[ClusterManager[MapMarker]] = None

  protected var cameraMoved = false

  private var markersMap: Map[Marker, MapMarker] = Map()

  private var distanceLine: Option[DistanceLine] = None
  private var polyLine: Option[Polyline] = None

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.map)

    ActivityLocationResolver.setBaseInterval(PidifrkySettings.gpsUpdateIntervals.map)

    checkPlayServices()

    Option(getSupportFragmentManager.findFragmentById(R.id.map)) match {
      case Some(f: SupportMapFragment) => f.getMapAsync(new OnMapReadyCallback {
        override def onMapReady(googleMap: GoogleMap): Unit = {
          map = Some(googleMap)

          googleMap.setIndoorEnabled(false)
          googleMap.setMyLocationEnabled(true)
          googleMap.setLocationSource(MapLocationSource)

          setMapType(PidifrkySettings.mapType, save = false)

          val uiSettings = googleMap.getUiSettings
          uiSettings.setZoomControlsEnabled(true)
          uiSettings.setCompassEnabled(true)

          val clm = new ClusterManager[MapMarker](BasicMapActivity.this, googleMap)
          //camera change listener called manually below!
          googleMap.setOnMarkerClickListener(clm)
          clm.setRenderer(new PidifrkyClusterRenderer(BasicMapActivity.this, googleMap, clm))

          clusterManager = Some(clm)

          googleMap.setOnMapLongClickListener(BasicMapActivity.this)

          LocationHandler.getCurrentLocation.foreach(MapLocationSource.apply) //show the position immediately!

          googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener {
            override def onInfoWindowClick(marker: Marker): Unit = {
              val mapMarker = markersMap.get(marker)

              DebugReporter.debug(s"Clicked on marker $mapMarker")

              mapMarker.foreach(onMapMarkerClick)
            }
          })

          //default camera view
          showDefaultView()

          BasicMapActivity.this.onMapReady(googleMap, getIntent, clm)

          googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
            def onCameraChange(cameraPosition: CameraPosition) {
              cameraMoved = true

              val bounds = googleMap.getProjection.getVisibleRegion.latLngBounds
              DebugReporter.debug("Map moved to " + bounds.getCenter + ", " + bounds.toString)

              //call cluster manager
              clm.onCameraChange(cameraPosition)

              BasicMapActivity.this.onCameraChange(cameraPosition)
            }
          })
        }
      })

      case Some(f) =>
        DebugReporter.debug(s"Requested SupportMapFragment, got ${f.getClass.getName}")

        InfoDialog('mapFailureWrongType, R.string.error, R.string.error_no_map).showAndThen { case _ =>
          this.finish()
        }

      case None =>
        DebugReporter.debug(s"Requested SupportMapFragment, got nothing")

        InfoDialog('mapFailureNone, R.string.error, R.string.error_no_map).showAndThen { case _ =>
          this.finish()
        }
    }
  }

  protected def showDefaultView(): Unit = map.foreach { googleMap =>
    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PidifrkyConstants.MAP_CENTER, 8f))
    cameraMoved = false
  }

  protected def displayItems(forced: Boolean, entities: Iterable[Entity], distanceLineTo: Option[LatLng] = None): Unit = {
    clearMap()

    val current = LocationHandler.getCurrentLocation.map(LocationHelper.toLatLng)

    distanceLineTo match {
      case Some(latLng) =>
        setDistanceLine(LineOptions(Color.RED, 5), latLng)

        if (forced || !cameraMoved) {
          current match {
            case Some(loc) => zoomTo(Seq(loc, latLng))
            case None => zoomTo(Seq(latLng))
          }
        }

      case None =>
        if (forced || !cameraMoved) {
          val points = (entities.toSeq.map(_.toLatLng) :+ current).flatten
          zoomTo(points)
        }
    }

    addMarkers(entities)
  }

  def onMapReady(map: GoogleMap, bundle: Intent, clusterManager: ClusterManager[MapMarker]): Unit

  def onMapMarkerClick(m: MapMarker): Unit

  def getMap: Option[GoogleMap] = map

  def clearMap(): Unit = {
    distanceLine.foreach(_.remove())
    polyLine.foreach(_.remove())
    clusterManager.foreach(_.clearItems())
    markersMap.synchronized {
      markersMap = Map()
    }
  }

  def setMapType(mapType: MapType, save: Boolean = true): Unit = {
    if (save) PidifrkySettings.withEditor(_.putInt("mapType", mapType.id))

    map.foreach(_.setMapType(mapType.id))
  }

  def centerMapToCurrent(): Unit = LocationHandler.getCurrentLocation.foreach(loc => centerMap(LocationHelper.toLatLng(loc)))

  def centerMap(point: LatLng): Unit = map.foreach { map =>
    val uiSettings = map.getUiSettings

    uiSettings.setAllGesturesEnabled(false)

    map.animateCamera(CameraUpdateFactory.newLatLng(point), 500, new GoogleMap.CancelableCallback() {
      def onFinish() = {
        uiSettings.setAllGesturesEnabled(true)
        cameraMoved = false
      }

      def onCancel() = uiSettings.setAllGesturesEnabled(true)
    })
  }

  def zoomTo(points: Seq[LatLng]): Unit = Utils.runOnUiThread {
    val bounds = LocationHelper.toLatLngBounds(points)

    getMap.foreach { map =>
      val size = Utils.getScreenSize(ctx)

      map.getUiSettings.setAllGesturesEnabled(false)

      map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, size.width, size.height, 250), 500, new CancelableCallback {
        override def onFinish(): Unit = {
          clusterManager.foreach(_.cluster())
          map.getUiSettings.setAllGesturesEnabled(true)
          cameraMoved = false
        }

        override def onCancel(): Unit = onFinish()
      })
    }
  }

  override def invalidateOptionsMenu(): Unit = {
    runOnUiThread {
      super.invalidateOptionsMenu()
    }
  }

  def addMarkers(entities: Iterable[Entity]): Unit = {
    clusterManager.foreach { clm =>
      entities.map(_.toMarker).foreach {
        case Some(marker) =>
          clm.addItem(marker)

        case _ => //non-displayable entity
      }
    }
  }

  def setPolyLine(options: LineOptions, points: IMapPoint*): Unit = map.foreach { map =>
    import scala.collection.JavaConverters._
    val seq = points.map(_.location.map(LocationHelper.toLatLng)).filter(_.isDefined).map(_.get)
    val polylineOptions = new PolylineOptions().addAll(seq.asJava).width(options.width).color(options.color)

    polyLine = Option(map.addPolyline(polylineOptions))
  }

  def setDistanceLine(options: LineOptions, entity: Entity): Unit = {
    for {
      map <- getMap
      location <- entity.location
      entityMarker <- entity.toMarker
      currentLocation <- LocationHandler.getCurrentLocation
    } yield {
      val polylineOptions = new PolylineOptions()
        .add(LocationHelper.toLatLng(location), LocationHelper.toLatLng(currentLocation))
        .width(options.width)
        .color(options.color)

      val icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("%.2f km".format(location.distanceTo(currentLocation) / 1000.0)))

      map.addMarker(new MarkerOptions().position(LocationHelper.getCenter(location, currentLocation)).icon(icon))

      val line = map.addPolyline(polylineOptions)
      val marker = map.addMarker(entityMarker.getMarkerOptions)

      distanceLine = Option(DistanceLine(line, marker))
    }
  }

  def setDistanceLine(options: LineOptions, point: LatLng): Unit = {
    distanceLine.foreach(_.remove())

    for {
      map <- getMap
      currentLocation <- LocationHandler.getCurrentLocation
    } yield {
      val polylineOptions = new PolylineOptions()
        .add(point, LocationHelper.toLatLng(currentLocation))
        .width(options.width)
        .color(options.color)

      import LocationHelper._

      val location: Location = point

      val icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("%.2f km".format(location.distanceTo(currentLocation) / 1000.0)))

      val line = map.addPolyline(polylineOptions)
      val marker = map.addMarker(new MarkerOptions().position(LocationHelper.getCenter(location, currentLocation)).icon(icon))

      distanceLine = Option(DistanceLine(line, marker))
    }
  }

  def isVisibleOnMap(location: Location): Boolean = {
    map.map(_.getProjection.getVisibleRegion.latLngBounds.contains(LocationHelper.toLatLng(location))).getOrElse(true)
  }

  private def checkPlayServices(): Unit = {
    val playServices = GoogleApiAvailability.getInstance
    val status = playServices.isGooglePlayServicesAvailable(getBaseContext)

    status match {
      case ConnectionResult.SUCCESS => //ok
      case _ =>
        val dialog = playServices.getErrorDialog(this, status, 10)
        dialog.setOnDismissListener(new OnDismissListener() {
          def onDismiss(dialog: DialogInterface) {
            BasicMapActivity.this.finish()
            System.exit(2)
          }
        })

        try {
          dialog.show()
        }
        catch {
          case e: Exception =>
            if (e.getMessage != null && !e.getMessage.startsWith("View not attached") && !e.getMessage.startsWith("Unable to add window")) {
              throw e
            }
        }
    }
  }

  private class PidifrkyClusterRenderer(ctx: Context, googleMap: GoogleMap, clusterManager: ClusterManager[MapMarker])
    extends DefaultClusterRenderer(ctx, googleMap, clusterManager) {

    override def onBeforeClusterItemRendered(item: MapMarker, markerOptions: MarkerOptions): Unit = {
      val options = item.getMarkerOptions

      markerOptions.title(options.getTitle)
      markerOptions.icon(options.getIcon)
      markerOptions.anchor(options.getAnchorU, options.getAnchorV)
    }

    override def onClusterItemRendered(clusterItem: MapMarker, marker: Marker): Unit = {
      super.onClusterItemRendered(clusterItem, marker)

      markersMap.synchronized {
        markersMap = markersMap + (marker -> clusterItem)
      }
    }
  }

}

case class LineOptions(color: Int, width: Int)

case class DistanceLine(line: Polyline, marker: Marker) {
  def remove(): Unit = {
    line.remove()
    marker.remove()
  }
}
