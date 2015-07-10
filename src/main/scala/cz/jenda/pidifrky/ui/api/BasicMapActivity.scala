package cz.jenda.pidifrky.ui.api

import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.location.Location
import android.os.Bundle
import android.view.Menu
import com.google.android.gms.common.{ConnectionResult, GooglePlayServicesUtil}
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener
import com.google.android.gms.maps.model._
import com.google.android.gms.maps.{CameraUpdateFactory, GoogleMap, OnMapReadyCallback, SupportMapFragment}
import com.google.maps.android.ui.IconGenerator
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.IMapPoint
import cz.jenda.pidifrky.data.pojo.Entity
import cz.jenda.pidifrky.logic.location.{ActivityLocationResolver, LocationHandler}
import cz.jenda.pidifrky.logic.map._
import cz.jenda.pidifrky.logic.{DebugReporter, PidifrkySettings, Toast}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicMapActivity extends BasicActivity with OnMapReadyCallback {
  private val iconGenerator = new IconGenerator(this)

  private var map: Option[GoogleMap] = None

  private var cameraMoved = false
  private var followLocation: Boolean = _


  override protected def actionBarMenu(): Option[Int] = Some(R.menu.map)

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.map)

    ActivityLocationResolver.setBaseInterval(PidifrkySettings.gpsUpdateIntervals.map)

    followLocation = PidifrkySettings.followLocationOnMap

    Option(savedInstanceState).foreach { bundle =>
    }

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

          BasicMapActivity.this.onMapReady(googleMap)

          LocationHandler.getCurrentLocation.foreach(MapLocationSource.apply) //show the position immediately!

          googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(49.8401903, 15.3693800), 8f))

          googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
            def onCameraChange(cameraPosition: CameraPosition) {
              val bounds = googleMap.getProjection.getVisibleRegion.latLngBounds
              DebugReporter.debug("Map moved to " + bounds.getCenter + ", " + bounds.toString)

              LocationHandler.getCurrentLocation.foreach { location =>
                if (followLocation) {
                  cameraMoved = true
                  if (!isVisibleOnMap(location)) {
                    followLocation = false

                    PidifrkySettings.editor.putBoolean("mapFollowPosition", followLocation).commit
                    Toast(R.string.map_following_off, Toast.Short)
                    runOnUiThread(invalidateOptionsMenu())
                  }
                }
              }
            }
          })
        }
      })
      case Some(f) => //TODO: warning
      case None => //TODO: warning
    }
  }

  override protected def onLocationChanged(location: Location): Unit = {
    super.onLocationChanged(location)

    if (followLocation) {
      centerMap(LocationHelper.toLatLng(location))
    }
  }

  override def onPrepareOptionsMenu(menu: Menu): Boolean = {
    menu.getItem(0).setVisible(mockLocation) //TODO
    //TODO menu.getItem(1).setVisible(getListActivityClass != null)
    menu.getItem(6).setChecked(followLocation)
    true
  }

  override protected def onActionBarClicked: PartialFunction[Int, Boolean] = {
    case R.id.menu_map_followPosition =>
      followLocation = !followLocation
      PidifrkySettings.editor.putBoolean("mapFollowPosition", followLocation).apply()

      runOnUiThread(invalidateOptionsMenu())

      if (followLocation) {
        centerMapToCurrent()
        Toast(R.string.map_following_on, Toast.Short)
      }
      else {
        Toast(R.string.map_following_off, Toast.Short)
      }

      true
    case R.id.menu_map_showNormal =>
      setMapType(NormalMapType)
      true
    case R.id.menu_map_showSattelite =>
      setMapType(SatteliteMapType)
      true
    case R.id.menu_map_showHybrid =>
      setMapType(HybridMapType)
      true
    case _ =>
      Toast("Not supported", Toast.Long)
      true
  }

  def getMap: Option[GoogleMap] = map

  def clearMap(): Unit = map.foreach(_.clear)

  def setMapType(mapType: MapType, save: Boolean = true): Unit = {
    if (save) PidifrkySettings.editor.putInt("mapType", mapType.id).apply()

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

  def isVisibleOnMap(location: Location): Boolean =
    map.map(_.getProjection.getVisibleRegion.latLngBounds.contains(LocationHelper.toLatLng(location))).getOrElse(true)


  private def checkPlayServices(): Unit = {
    val status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext)

    status match {
      case ConnectionResult.SUCCESS => //ok
      case _ =>
        val dialog = GooglePlayServicesUtil.getErrorDialog(status, this, 10)
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

case class LineOptions(color: Int, width: Int)