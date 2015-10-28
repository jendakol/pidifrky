package cz.jenda.pidifrky.data.pojo

import android.database.Cursor
import android.location.Location
import android.net.Uri
import cz.jenda.pidifrky.data.CardTiles
import cz.jenda.pidifrky.logic.map.{CardMapMarker, MapMarker}
import cz.jenda.pidifrky.logic.{Application, ImageHandler}

import scala.util.Try

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
case class Card(id: Int, number: Int, name: String, nameRaw: String, state: CardState, location: Option[Location], hasImage: Boolean, neighboursIds: Seq[Option[Int]], merchantsIds: Seq[Int]) extends Entity {

  def getFullImageUri: Option[Uri] = ImageHandler.getFullImageUri(id)

  def getThumbImageUri: Option[Uri] = ImageHandler.getThumbImageUri(id)


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
      hasImage = cursor.getInt(4) > 0, //TODO: while programming "start without downloading files", this won't be automatically Some() anymore
      merchantsIds = parseIds(cursor.getString(7)),
      neighboursIds = parseIdsOption(cursor.getString(8)),
      state = toState(9)(cursor)
    )
  }

  private def toState(index: Int)(cursor: Cursor): CardState = {
    CardState.values()(if (cursor.getType(index) == Cursor.FIELD_TYPE_INTEGER) cursor.getInt(index) else 0)
  }
}
