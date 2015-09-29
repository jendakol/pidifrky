package cz.jenda.pidifrky.data.pojo

import android.database.Cursor
import android.location.{LocationManager, Location}
import com.google.common.primitives.Ints
import cz.jenda.pidifrky.data.IMapPoint
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.logic.map.MapMarker
import org.apache.commons.lang3.StringUtils

import scala.util.Try

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait Entity extends IMapPoint {
  val id: Int

  val name: String

  val nameRaw: String

  val location: Option[Location]

  def getDistance(destLocation: Location): Option[Double] = location.map(destLocation.distanceTo)

  def getDistance: Option[Double] = for {
    loc <- location
    current <- LocationHandler.getCurrentLocation
  } yield loc.distanceTo(current)

  def toMarker: Option[MapMarker]
}

trait EntityFactory[E <: Entity] {
  def create(c: Cursor): Try[E]

  protected def toLocation(latIndex: Int, lonIndex: Int)(implicit cursor: Cursor): Option[Location] = {
    if (cursor.getType(latIndex) != Cursor.FIELD_TYPE_NULL && cursor.getType(lonIndex) != Cursor.FIELD_TYPE_NULL) {
      val l = new Location(LocationManager.GPS_PROVIDER)
      l.setLatitude(cursor.getDouble(latIndex))
      l.setLongitude(cursor.getDouble(lonIndex))
      Some(l)
    } else None
  }

  protected def parseIdsOption(t: String): List[Option[Int]] =
    t.split(",").toList
      .map(i =>
      if (StringUtils.isNotBlank(i))
        Try(Ints.tryParse(i).toInt).toOption
      else
        None
      )

  protected def parseIds(t: String): List[Int] = parseIdsOption(t).flatten
}