package cz.jenda.pidifrky.logic.map

import android.location.{Location, LocationManager}
import com.google.android.gms.maps.model.LatLng

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object LocationHelper {
  def toLatLng(location: Location): LatLng = new LatLng(location.getLatitude, location.getLongitude)

  def toLocation(latitude: Double, longitude: Double, source: LocationSource = GpsSource): Location = {
    val l = new Location(source.name)
    l.setLatitude(latitude)
    l.setLongitude(longitude)
    l
  }

  def getCenter(location1: Location, location2: Location): LatLng = {
    val center: Location = new Location(GpsSource.name)

    center.setLatitude(if (location1 != null && location2 != null) (location1.getLatitude + location2.getLatitude) / 2.0 else if (location1 != null) location1.getLatitude else location2.getLatitude)
    center.setLongitude(if (location1 != null && location2 != null) (location1.getLongitude + location2.getLongitude) / 2.0 else if (location1 != null) location1.getLongitude else location2.getLongitude)

    toLatLng(center)
  }
}

sealed trait LocationSource {
  val name: String
}

case object GpsSource extends LocationSource {
  override val name: String = LocationManager.GPS_PROVIDER
}

case object NetworkSource extends LocationSource {
  override val name: String = LocationManager.NETWORK_PROVIDER
}