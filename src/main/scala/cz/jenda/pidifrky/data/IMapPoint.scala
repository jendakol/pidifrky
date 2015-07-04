package cz.jenda.pidifrky.data

import android.location.Location

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait IMapPoint {
  val gps: Option[Location]

  override def toString: String = {
    "IMapPoint {" + gps + "}"
  }
}