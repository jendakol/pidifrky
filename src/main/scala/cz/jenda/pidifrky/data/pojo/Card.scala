package cz.jenda.pidifrky.data.pojo

import android.location.Location
import cz.jenda.pidifrky.data.{CardOwnedState, CardState, CardTiles, CardWantedState}
import cz.jenda.pidifrky.logic.Application
import cz.jenda.pidifrky.logic.map.{CardMapMarker, MapMarker}

/**
 * Created <b>15.3.13</b><br>
 *
 * @author Jenda Kolena, jendakolena@gmail.com
 * @version 0.1
 * @since 0.2
 */
object Card {
  //  def getCard(cursor: Cursor): Card = {
  //    if (cursor != null && cursor.getCount > 0) new Card(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getDouble(4), cursor.getDouble(5), cursor.getString(6), cursor.getString(7)) else null
  //  }

}

case class Card(id: Int, number: Int, name: String, nameRaw: String, gps: Option[Location], image: String, neighbours: String) extends Entity {

  private var state: Option[CardState] = Some(CardWantedState) //TODO

  //  def getFullImage: Bitmap = {
  //    try {
  //      return BitmapFactory.decodeFile(Utils.getFullImageUri(this).getEncodedPath)
  //    }
  //    catch {
  //      case e: NullPointerException => {
  //        return null
  //      }
  //    }
  //  }


  //  def getMerchants: String = {
  //    return merchants
  //  }
  //
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


  def getDistance(location: Location): Option[Double] = gps.map(location.distanceTo)

  def getDistance: Option[Double] = gps.map(gps => gps.distanceTo(gps)) //TODO

  def getState: CardState = state.getOrElse {
    CardWantedState //TODO: cardsdao
  }

  def isOwner: Boolean = {
    state.getOrElse {
      CardWantedState //TODO: cardsdao
    } == CardOwnedState
  }

  def isWanted: Boolean = {
    state.getOrElse {
      CardWantedState //TODO: cardsdao
    } == CardWantedState
  }

  //
  //
  //  def isOwner: Boolean = {
  //    return if (owner == null) {
  //      ({
  //        owner = CardsDao.getInstance(Utils.getContext).isOwner(this);
  //        owner
  //      })
  //    }
  //    else {
  //      owner
  //    }
  //  }
  //
  //  def isOwner(refreshState: Boolean): Boolean = {
  //    if (refreshState) owner = null
  //    return isOwner
  //  }
  //
  //  def setOwner(owner: Boolean) {
  //    CardsDao.getInstance(Utils.getContext).setOwner(this, owner)
  //    if (owner) setWanted(false)
  //    this.owner = owner
  //  }
  //
  //  def isWanted: Boolean = {
  //    return if (wanted == null) {
  //      ({
  //        wanted = CardsDao.getInstance(Utils.getContext).isWanted(this);
  //        wanted
  //      })
  //    }
  //    else {
  //      wanted
  //    }
  //  }
  //
  //  def isWanted(refreshState: Boolean): Boolean = {
  //    if (refreshState) wanted = null
  //    return isWanted
  //  }
  //
  //  def setWanted(wanted: Boolean) {
  //    CardsDao.getInstance(Utils.getContext).setWanted(this, wanted)
  //    this.wanted = wanted
  //  }


  def getNeighboursTable: Option[CardTiles] = Application.currentActivity.map(new CardTiles(_, this))


  override def equals(o: Any): Boolean = {
    if (this == o) return true
    if (o == null || (getClass ne o.getClass)) return false
    val card = o.asInstanceOf[Card]
    id == card.id && number == card.number && !(if (gps != null) !(gps == card.gps) else card.gps != null) && !(if (image != null) !(image == card.image) else card.image != null)
    //    && !(if (merchants != null) !(merchants == card.merchants) else card.merchants != null)
    //    && ! (if (neighbours != null) !(neighbours == card.neighbours) else card.neighbours != null) && (name == card.name)
  }

  override def hashCode: Int = {
    var result: Int = id
    result = 31 * result + number
    result = 31 * result + name.hashCode
    result = 31 * result + (if (image != null) image.hashCode else 0)
    result = 31 * result + (if (gps != null) gps.hashCode else 0)
    //    result = 31 * result + (if (merchants != null) merchants.hashCode else 0)
    result
  }

  override def toMarker: Option[MapMarker] = gps.map(_ => CardMapMarker(this))
}