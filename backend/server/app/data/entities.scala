package data

/**
 * @author Jenda Kolena, kolena@avast.com
 */
case class CardPojo(id: Int, number: Int, name: String, latitude: Option[Float], longitude: Option[Float], neighboursIds: String)

case class MerchantPojo(id: Int, name: String, address: String, latitude: Option[Float], longitude: Option[Float], gps: Int)

case class Card_x_MerchantPojo(id: Option[Int], cardId: Int, merchantId: Int)
