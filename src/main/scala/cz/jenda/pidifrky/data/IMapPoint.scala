package cz.jenda.pidifrky.data

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import cz.jenda.pidifrky.logic.map.LocationHelper

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait IMapPoint {
  val gps: Option[Location]

  val name: String

  def toLatLng: Option[LatLng] = gps.map(LocationHelper.toLatLng)

  override def toString: String = {
    "IMapPoint {" + gps + "}"
  }
}