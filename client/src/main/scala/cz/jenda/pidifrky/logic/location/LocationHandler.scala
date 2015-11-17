package cz.jenda.pidifrky.logic.location

import java.util.concurrent.atomic.AtomicBoolean

import android.app.Activity
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic._
import cz.jenda.pidifrky.logic.map.{LocationHelper, LocationSource, StoredSource}
import cz.jenda.pidifrky.ui.api.ElementId
import io.nlopez.smartlocation.location.providers.LocationBasedOnActivityProvider
import io.nlopez.smartlocation.{OnLocationUpdatedListener, SmartLocation}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object LocationHandler {
  type LocationListener = Location => Unit

  private var listeners: Map[ElementId, LocationListener] = Map()

  private var currentLocation: Option[Location] = None

  private var mocking = false

  private var lastSource: Option[LocationSource] = None

  private val activityProvider = new LocationBasedOnActivityProvider(ActivityLocationResolver)

  private val tracking = new AtomicBoolean(false)

  def start(implicit ctx: Activity): Unit = if (tracking.compareAndSet(false, true)) {
    if (currentLocation.isEmpty) {
      //probably first attempt to lock
      Toast(R.string.locating, Toast.Short)
    }

    mocking = false

    val control = SmartLocation
      .`with`(ctx)
      .location(activityProvider)
      .continuous()

    //TODO: check if any provider is available

    Option(control.getLastLocation) //can be null!
      .map(loc => {
      loc.setAccuracy(1000) //last known location, can be not precise
      loc
    }).foreach(updateLocation)

    control.start(new OnLocationUpdatedListener {
      override def onLocationUpdated(location: Location): Unit = Option(location) match {
        case Some(loc) => updateLocation(loc)
        case None => DebugReporter.debug("Received null location")
      }
    })
  }

  def stop(implicit ctx: Activity): Unit = if (tracking.compareAndSet(true, false)) {
    SmartLocation.`with`(ctx).location().stop()
  }

  protected def updateLocation(location: Location)(implicit ctx: Activity): Unit = {
    currentLocation match {
      case Some(oldLocation) =>
        val dist = location.distanceTo(oldLocation)
        if (Math.abs(location.getAccuracy - oldLocation.getAccuracy) > dist && dist < 5) {
          GpsLogger.addEvent("Location: " + Format(location, dist))
          GpsLogger.addEvent("Found location does NOT have sufficient accuracy")
          return
        }

        GpsLogger.addEvent("Location: " + Format(location, dist))
      case _ => GpsLogger.addEvent("Location: " + Format(location, 4))
    }

    currentLocation = Some(location)

    val locationSource = LocationSource(location.getProvider)

    lastSource match {
      case Some(source) if source.name != location.getProvider =>
        if (source == StoredSource) Toast(R.string.location_found, Toast.Medium) //loaded first "not stored" location
        lastSource = Some(locationSource)
      case None =>
        lastSource = Some(locationSource)
        if (locationSource != StoredSource) Toast(R.string.location_found, Toast.Medium)
      case _ =>
    }

    listeners.values.foreach { listener =>
      listener(location)
    }
  }

  def mockLocation(latLng: LatLng)(implicit ctx: Activity): Unit = {
    SmartLocation.`with`(ctx).location().stop()
    mocking = true
    val location = LocationHelper.toLocation(latLng)
    GpsLogger.addEvent("Mocking: " + Format(location))
    Toast("Mocking location " + location, ToastButton.UNDO {
      disableMocking
    }, Toast.Short)
    currentLocation = Some(location)
    updateLocation(location)
  }

  def disableMocking(implicit ctx: Activity): Unit = start

  def isMockingLocation: Boolean = mocking

  def getCurrentLocation: Option[Location] = currentLocation

  def addListener(listener: LocationListener)(implicit id: ElementId): Unit = {
    listeners = listeners + (id -> listener)
  }

  def removeListener(implicit id: ElementId): Unit = {
    listeners = listeners - id
  }
}
