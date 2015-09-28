package cz.jenda.pidifrky.data.pojo

import android.database.Cursor
import android.location.Location
import cz.jenda.pidifrky.data.IMapPoint
import cz.jenda.pidifrky.logic.map.MapMarker

import scala.util.Try

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait Entity extends IMapPoint {
  val id: Int

  val name: String

  val nameRaw: String

  val location: Option[Location]

  def getDistance: Option[Double]

  def getDistance(location: Location): Option[Double]

  def toString: String

  def toMarker: Option[MapMarker]
}

trait EntityFactory[E <: Entity] {
  def create(c: Cursor): Try[E]
}