package cz.jenda.pidifrky.data.pojo

import android.database.Cursor
import android.graphics.{Bitmap, BitmapFactory}
import android.location.{Location, LocationManager}
import com.google.common.primitives.Ints
import cz.jenda.pidifrky.data.CardTiles
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.logic.map.{CardMapMarker, MapMarker}
import cz.jenda.pidifrky.logic.{Application, Utils}
import org.apache.commons.lang3.StringUtils

import scala.util.Try

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
case class Card(id: Int, number: Int, name: String, nameRaw: String, state: CardState, location: Option[Location], image: Option[String], neighboursIds: List[Option[Int]], merchantsIds: List[Int]) extends Entity {

  def getFullImage: Option[Bitmap] = image.flatMap(Utils.getFullImageUri).map { im =>
    try {
      BitmapFactory.decodeFile(im.getEncodedPath)
    }
    catch {
      case e: NullPointerException =>
        null
    }
  }


  //  def getMerchantsList: ArrayList[Merchant] = {
  //    val merchants: ArrayList[Merchant] = new ArrayList[Merchant]
  //    for (idS <- this.merchants.split(",")) {
  //      try {
  //        if ("" == idS) continue //todo: continue is not supported
  //        val id: Int = idS.toInt
  //        val m: Merchant = MerchantsDao.getInstance.get(id).asInstanceOf[Merchant]
  //        if (m != null) {
  //          merchants.add(m)
  //        }
  //      }
  //      catch {
  //        case e: NumberFormatException => {
  //        }
  //        case e: IllegalAccessException => {
  //          throw new RuntimeException(e)
  //        }
  //      }
  //    }
  //    merchants
  //  }


  def getDistance(destLocation: Location): Option[Double] = location.map(destLocation.distanceTo)

  def getDistance: Option[Double] = for {
    loc <- location
    current <- LocationHandler.getCurrentLocation
  } yield loc.distanceTo(current)


  def isOwner: Boolean = {
    state == CardState.OWNED
  }

  def isWanted: Boolean = {
    state == CardState.WANTED
  }

  def getNeighboursTable: Option[CardTiles] = Application.currentActivity.map(new CardTiles(_, this))

  override def toMarker: Option[MapMarker] = location.map(_ => CardMapMarker(this))
}

object Card extends EntityFactory[Card] {
  override def create(cursor: Cursor): Try[Card] = Try {
    Card(
      id = cursor.getInt(0),
      number = cursor.getInt(1),
      name = cursor.getString(2),
      nameRaw = cursor.getString(3),
      location = toLocation(5, 6)(cursor),
      image = Some(cursor.getString(4)), //TODO: while programming "start without downloading files", this won't be automatically Some() anymore
      merchantsIds = parseIds(cursor.getString(7)),
      neighboursIds = parseIdsOption(cursor.getString(8)),
      state = toState(9)(cursor)
    )
  }

  private def toState(index: Int)(cursor: Cursor): CardState = {
    CardState.values()(if (cursor.getType(index) == Cursor.FIELD_TYPE_INTEGER) cursor.getInt(index) else 0)
  }

  private def toLocation(latIndex: Int, lonIndex: Int)(implicit cursor: Cursor): Option[Location] = {
    if (cursor.getType(latIndex) != Cursor.FIELD_TYPE_NULL && cursor.getType(lonIndex) != Cursor.FIELD_TYPE_NULL) {
      val l = new Location(LocationManager.GPS_PROVIDER)
      l.setLatitude(cursor.getDouble(latIndex))
      l.setLongitude(cursor.getDouble(lonIndex))
      Some(l)
    } else None
  }

  private def parseIdsOption(t: String): List[Option[Int]] =
    t.split(",").toList
      .map(i =>
      if (StringUtils.isNotBlank(i))
        Try(Ints.tryParse(i).toInt).toOption
      else
        None
      )

  private def parseIds(t: String): List[Int] = parseIdsOption(t).flatten
}
