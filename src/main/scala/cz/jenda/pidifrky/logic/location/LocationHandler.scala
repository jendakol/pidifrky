package cz.jenda.pidifrky.logic.location

import android.app.Activity
import android.location.Location
import com.google.android.gms.maps.model.LatLng
import cz.jenda.pidifrky.logic.map.{LocationHelper, LocationSource}
import cz.jenda.pidifrky.logic.{DebugReporter, PidifrkySettings, Toast, ToastButton}
import io.nlopez.smartlocation.location.providers.LocationBasedOnActivityProvider
import io.nlopez.smartlocation.{OnLocationUpdatedListener, SmartLocation}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object LocationHandler {

  private var listeners: Set[LocationListener] = Set()

  private var currentLocation: Option[Location] = None

  private var mocking = false

  private var interval: Option[Int] = None
  private var lastSource: Option[LocationSource] = None

  private val activityProvider = new LocationBasedOnActivityProvider(ActivityLocationResolver)

  def start(interval: Int)(implicit ctx: Activity): Unit = {
    this.interval = Some(interval)

    mocking = false

    val control = SmartLocation
      .`with`(ctx)
      .location()
      .provider(activityProvider)

    currentLocation = Option(control.getLastLocation) //can be null!

    control.start(new OnLocationUpdatedListener {
      override def onLocationUpdated(location: Location): Unit = updateLocation(location)
    })
  }

  protected def updateLocation(location: Location)(implicit ctx: Activity): Unit = if (location != null) {
    DebugReporter.debug("Received location: " + location)

    lastSource match {
      case Some(source) if source.name != location.getProvider =>
        lastSource = Some(LocationSource(location.getProvider))
      case None => lastSource = Some(LocationSource(location.getProvider))
      case _ =>
    }

    //    Toast("Location has been changed to " + location)

    listeners.foreach { listener =>
      listener.onLocationChanged(location)
    }
  }
  else {
    DebugReporter.debug("Received null location")
  }

  def getSetInterval: Option[Int] = interval

  def mockLocation(latLng: LatLng)(implicit ctx: Activity): Unit = {
    SmartLocation.`with`(ctx).location().stop()
    mocking = true
    val location = LocationHelper.toLocation(latLng)
    DebugReporter.debug("Mocking location " + location)
    Toast("Mocking location " + location, ToastButton.UNDO {
      disableMocking
    }, Toast.Short)
    currentLocation = Some(location)
    updateLocation(location)
  }

  def disableMocking(implicit ctx: Activity): Unit = start(interval.getOrElse(PidifrkySettings.gpsUpdateInterval))

  def isMockingLocation: Boolean = mocking

  def addListener(listener: LocationListener): Unit = {
    listeners = listeners + listener
  }

  def removeListener(listener: LocationListener): Unit = {
    listeners = listeners - listener
  }


}

trait LocationListener {
  def onLocationChanged(location: Location): Unit
}