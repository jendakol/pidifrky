package cz.jenda.pidifrky.data.pojo

import android.location.Location
import cz.jenda.pidifrky.data.IMapPoint
import cz.jenda.pidifrky.logic.map.MapMarker

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait Entity extends IMapPoint {
  val id: Int

  val name: String

  val nameRaw: String

  val gps: Option[Location]

  def getDistance: Option[Double]

  def getDistance(location: Location): Option[Double]

  def toString: String

  def toMarker: Option[MapMarker]
}