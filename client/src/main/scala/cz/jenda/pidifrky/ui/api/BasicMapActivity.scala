package cz.jenda.pidifrky.ui.api

import android.content.DialogInterface.OnDismissListener
import android.content.{Context, DialogInterface, Intent}
import android.location.Location
import android.os.Bundle
import com.google.android.gms.common.{ConnectionResult, GoogleApiAvailability}
import com.google.android.gms.maps.GoogleMap.{OnCameraChangeListener, OnMapLongClickListener}
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
import cz.jenda.pidifrky.logic.{DebugReporter, PidifrkySettings}
import cz.jenda.pidifrky.ui.dialogs.InfoDialog

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicMapActivity extends BasicActivity with OnMapLongClickListener with OnCameraChangeListener {
  private lazy val iconGenerator = new IconGenerator(this)

  private var map: Option[GoogleMap] = None
  private var clusterManager: Option[ClusterManager[MapMarker]] = None

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

          //default camera view
          googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(BasicMapActivity.DefaultLatLng, 8f))

          BasicMapActivity.this.onMapReady(googleMap, getIntent, clm)

          googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
            def onCameraChange(cameraPosition: CameraPosition) {
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

  def onMapReady(map: GoogleMap, bundle: Intent, clusterManager: ClusterManager[MapMarker]): Unit

  def getMap: Option[GoogleMap] = map

  def clearMap(): Unit = map.foreach(_.clear)

  def setMapType(mapType: MapType, save: Boolean = true): Unit = {
    if (save) PidifrkySettings.withEditor(_.putInt("mapType", mapType.id))

    map.foreach(_.setMapType(mapType.id))
  }

  def centerMap(point: LatLng): Unit = map.foreach { map =>
    val uiSettings = map.getUiSettings

    uiSettings.setAllGesturesEnabled(false)

    map.animateCamera(CameraUpdateFactory.newLatLng(point), 500, new GoogleMap.CancelableCallback() {
      def onFinish() = uiSettings.setAllGesturesEnabled(true)

      def onCancel() = uiSettings.setAllGesturesEnabled(true)
    })
  }

  def centerMapToCurrent(): Unit = LocationHandler.getCurrentLocation.foreach(loc => centerMap(LocationHelper.toLatLng(loc)))

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

  def addLine(options: LineOptions, points: IMapPoint*): Unit = map foreach { map =>
    import scala.collection.JavaConverters._
    val seq = points.map(_.location.map(LocationHelper.toLatLng)).filter(_.isDefined).map(_.get)
    val polyline = new PolylineOptions().addAll(seq.asJava).width(options.width).color(options.color)
    map.addPolyline(polyline)
  }

  def addDistanceLine(options: LineOptions, entity: Entity): Unit = for {
    map <- getMap
    location <- entity.location
    marker <- entity.toMarker
    currentLocation <- LocationHandler.getCurrentLocation
  } yield {
      val polyline = new PolylineOptions()
        .add(LocationHelper.toLatLng(location), LocationHelper.toLatLng(currentLocation))
        .width(options.width)
        .color(options.color)

      map.addPolyline(polyline)

      val icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon("%.2f km".format(location.distanceTo(currentLocation) / 1000.0)))

      map.addMarker(new MarkerOptions().position(LocationHelper.getCenter(location, currentLocation)).icon(icon))

      map.addMarker(marker.getMarkerOptions)
    }

  def isVisibleOnMap(location: Location): Boolean =
    map.map(_.getProjection.getVisibleRegion.latLngBounds.contains(LocationHelper.toLatLng(location))).getOrElse(true)


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
}

object BasicMapActivity {
  final val DefaultLatLng = new LatLng(49.8401903, 15.3693800)
}

private class PidifrkyClusterRenderer(ctx: Context, googleMap: GoogleMap, clusterManager: ClusterManager[MapMarker])
  extends DefaultClusterRenderer(ctx, googleMap, clusterManager) {

  override def onBeforeClusterItemRendered(item: MapMarker, markerOptions: MarkerOptions): Unit = {
    val options = item.getMarkerOptions

    markerOptions.title(options.getTitle)
    markerOptions.icon(options.getIcon)
    markerOptions.anchor(options.getAnchorU, options.getAnchorV)
  }
}

case class LineOptions(color: Int, width: Int)