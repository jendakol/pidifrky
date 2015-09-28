package cz.jenda.pidifrky.data

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import cz.jenda.pidifrky.logic.map.LocationHelper

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait IMapPoint {
  val location: Option[Location]

  val name: String

  def toLatLng: Option[LatLng] = location.map(LocationHelper.toLatLng)
}