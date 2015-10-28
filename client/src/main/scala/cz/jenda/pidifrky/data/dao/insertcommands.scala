package cz.jenda.pidifrky.data.dao

import cz.jenda.pidifrky.data.pojo.{Card, Merchant}
import cz.jenda.pidifrky.data.{CardsTable, MerchantsTable}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
sealed trait InsertCommand {
  def query: String

  def args: Array[AnyRef]
}

case class CardInsertCommand(card: Card) extends InsertCommand {

  import card._

  override def query: String = s"insert into ${CardsTable.NAME} values (?, ?, ?, ?, ?, ?, ?, ?, ?)"

  private val (lat, lon) = location.map(l => (l.getLatitude.toString, l.getLongitude.toString)).getOrElse(("", ""))

  override def args: Array[AnyRef] =
    Array(id.toString, number.toString, name, nameRaw, (if (hasImage) 1 else 0).toString, lat, lon, merchantsIds.mkString(","), neighboursIds.map(_.getOrElse(0)).mkString(","))
}

case class MerchantInsertCommand(merchant: Merchant) extends InsertCommand {

  import merchant._

  override def query: String = s"insert into ${MerchantsTable.NAME} values (?, ?, ?, ?, ?, ?, ?, ?)"

  private val (lat, lon) = location.map(l => (l.getLatitude.toString, l.getLongitude.toString)).getOrElse(("", ""))

  override def args: Array[AnyRef] =
    Array(id.toString, name, nameRaw, address, lat, lon, (if (merchantLocation.precise) 1 else 0).toString, cardsIds.mkString(","))
}

case class CardToMerchantsLinkUpdateCommand(cardId: Int, merchants: Seq[Int]) extends InsertCommand {
  override def query: String = s"update ${CardsTable.NAME} set ${CardsTable.COL_MERCHANTS_IDS} = ? where id = ?"

  override def args: Array[AnyRef] = Array(merchants.mkString(","), cardId.toString)
}

case class MerchantToCardsLinkUpdateCommand(merchantId: Int, cards: Seq[Int]) extends InsertCommand {
  override def query: String = s"update ${MerchantsTable.NAME} set ${MerchantsTable.COL_CARDS_IDS} = ? where id = ?"

  override def args: Array[AnyRef] = Array(cards.mkString(","), merchantId.toString)
}
