package cz.jenda.pidifrky.logic.map

import android.location.{Location, LocationManager}
import com.google.android.gms.maps.model.LatLng

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object LocationConversions {
  def toLatLng(location: Location): LatLng = new LatLng(location.getLatitude, location.getLongitude)

  def toLocation(latitude: Double, longitude: Double, source: LocationSource = GpsSource): Location = {
    val l = new Location(source.name)
    l.setLatitude(latitude)
    l.setLongitude(longitude)
    l
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