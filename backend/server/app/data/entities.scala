package data

/**
  * @author Jenda Kolena, jendakolena@gmail.com
  */
case class CardPojo(id: Int, number: Int, name: String, latitude: Option[Float], longitude: Option[Float], neighboursIds: String)

case class MerchantPojo(id: Int, name: String, address: String, latitude: Option[Float], longitude: Option[Float], gps: Int)

case class Card_x_MerchantPojo(id: Option[Int], cardId: Int, merchantId: Int)


object PojoFactory {
  def createCard(id: Int, number: Int, name: String, location: Option[(Float, Float)], neighboursIds: String): CardPojo = {
    val (lat, lon) = location.map { case (la, lo) => (Option(la), Option(lo)) }.getOrElse((None, None))

    CardPojo(id, number, name, lat, lon, neighboursIds)
  }

  def createMerchant(id: Int, name: String, address: String, location: Option[(Float, Float)], precise: Boolean): MerchantPojo = {
    val (lat, lon) = location.map { case (la, lo) => (Option(la), Option(lo)) }.getOrElse((None, None))

    MerchantPojo(id, name, address, lat, lon, if (precise) 1 else 0)
  }
}