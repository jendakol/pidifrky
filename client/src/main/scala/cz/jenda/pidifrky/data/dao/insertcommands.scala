package cz.jenda.pidifrky.data.dao

import cz.jenda.pidifrky.{CardsTable, MerchantsTable}

/**
 * @author Jenda Kolena, kolena@avast.com
 */
sealed trait InsertCommand {
  def query: String

  def args: Array[AnyRef]
}

case class CardInsertCommand(id: Int, number: Int, name: String, nameRaw: String, gpsLat: Double, gpsLon: Double, image: Option[String], neighboursIds: String, merchantsIds: String) extends InsertCommand {
  override def query: String = s"insert into ${CardsTable.NAME} values (?, ?, ?, ?, ?, ?, ?, ?, ?)"

  override def args: Array[AnyRef] =
    Array(id.toString, number.toString, name, nameRaw, image.orNull, gpsLat.toString, gpsLon.toString, merchantsIds, neighboursIds)
}

case class MerchantInsertCommand(id: Int, name: String, nameRaw: String, address: String, gpsLat: Double, gpsLon: Double, preciseLocation: Boolean) extends InsertCommand {
  override def query: String = s"insert into ${MerchantsTable.NAME} values (?, ?, ?, ?, ?, ?, ?)"

  override def args: Array[AnyRef] =
    Array(id.toString, name, nameRaw, address, gpsLat.toString, gpsLon.toString, (if (preciseLocation) 1 else 0).toString)
}
