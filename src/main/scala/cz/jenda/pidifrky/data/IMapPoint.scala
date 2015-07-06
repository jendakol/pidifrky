package cz.jenda.pidifrky.data

import android.location.Location

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait IMapPoint {
  val gps: Option[Location]

  val name: String

  override def toString: String = {
    "IMapPoint {" + gps + "}"
  }
}