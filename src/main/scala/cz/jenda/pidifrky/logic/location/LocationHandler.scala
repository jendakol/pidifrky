package cz.jenda.pidifrky.logic.location

import android.app.Activity
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic._
import cz.jenda.pidifrky.logic.map.{LocationHelper, LocationSource, StoredSource}
import io.nlopez.smartlocation.location.providers.LocationBasedOnActivityProvider
import io.nlopez.smartlocation.{OnLocationUpdatedListener, SmartLocation}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object LocationHandler {
  type LocationListener = Location => Unit

  private var listeners: Set[LocationListener] = Set()

  private var currentLocation: Option[Location] = None

  private var mocking = false

  private var lastSource: Option[LocationSource] = None

  private val activityProvider = new LocationBasedOnActivityProvider(ActivityLocationResolver)

  def start(implicit ctx: Activity): Unit = {

    if (currentLocation.isEmpty) {
      //probably first attempt to lock
      Toast(R.string.locating, Toast.Short)
    }

    mocking = false

    val control = SmartLocation
      .`with`(ctx)
      .location()
      .provider(activityProvider)

    //TODO: check if any provider is available

    currentLocation = Option(control.getLastLocation) //can be null!
      .map(loc => {
      loc.setAccuracy(1000) //last known location, can be not precise
      loc
    })

    currentLocation.foreach(updateLocation)

    control.start(new OnLocationUpdatedListener {
      override def onLocationUpdated(location: Location): Unit = updateLocation(location)
    })
  }

  protected def updateLocation(location: Location)(implicit ctx: Activity): Unit = if (location != null) {
    GpsLogger.addEvent("Location: " + Format(location, 4))

    val locationSource = LocationSource(location.getProvider)

    lastSource match {
      case Some(source) if source.name != location.getProvider =>
        lastSource = Some(locationSource)
      case None =>
        lastSource = Some(locationSource)
        if (locationSource != StoredSource) Toast(R.string.location_found, Toast.Medium)
      case _ =>
    }

    listeners.foreach { listener =>
      listener(location)
    }
  }
  else {
    DebugReporter.debug("Received null location")
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

  def addListener(listener: LocationListener): Unit = {
    listeners = listeners + listener
  }

  def removeListener(listener: LocationListener): Unit = {
    listeners = listeners - listener
  }
}
