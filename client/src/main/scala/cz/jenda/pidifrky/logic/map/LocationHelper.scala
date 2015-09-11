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

  def toLocation(latLng: LatLng): Location = {
    toLocation(latLng.latitude, latLng.longitude, MockedSource)
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

object LocationSource {
  final val Fused = "fused"
  final val Mocked = "mocked"
  final val Stored = "LocationStore"

  import LocationManager._

  def apply(name: String): LocationSource = name match {
    case GPS_PROVIDER => GpsSource
    case NETWORK_PROVIDER | PASSIVE_PROVIDER => NetworkSource
    case Fused => FusedSource
    case Mocked => MockedSource
    case Stored => StoredSource
  }
}

case object GpsSource extends LocationSource {
  override val name: String = LocationManager.GPS_PROVIDER
}

case object NetworkSource extends LocationSource {
  override val name: String = LocationManager.NETWORK_PROVIDER
}

case object FusedSource extends LocationSource {
  override val name: String = LocationSource.Fused
}

case object MockedSource extends LocationSource {
  override val name: String = LocationSource.Mocked
}

case object StoredSource extends LocationSource {
  override val name: String = LocationSource.Stored
}