package cz.jenda.pidifrky.data.pojo

import android.database.Cursor
import android.location.Location
import cz.jenda.pidifrky.logic.map.{MapMarker, MerchantMapMarker}

import scala.util.Try

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
case class Merchant(id: Int, name: String, nameRaw: String, address: String, merchantLocation: MerchantLocation, cardsIds: Seq[Int]) extends Entity {

  val location: Option[Location] = merchantLocation.location

  override def toMarker: Option[MapMarker] = location.map(_ => MerchantMapMarker(this))
}

case class MerchantLocation(location: Option[Location], precise: Boolean)

object Merchant extends EntityFactory[Merchant] {
  override def create(c: Cursor): Try[Merchant] = Try {
    Merchant(
      id = c.getInt(0),
      name = c.getString(1),
      nameRaw = c.getString(2),
      address = c.getString(3),
      merchantLocation = MerchantLocation(toLocation(4, 5)(c), c.getInt(6) > 0),
      cardsIds = parseIds(c.getString(7))
    )
  }
}
