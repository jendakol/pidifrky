package cz.jenda.pidifrky.data.dao

import cz.jenda.pidifrky.CardsTable

/**
 * @author Jenda Kolena, kolena@avast.com
 */
sealed trait InsertCommand {
  def query: String

  def args: Array[AnyRef]
}

case class CardInsertCommand(id: Int, number: Int, name: String, nameRaw: String, gpsLat: Double, gpsLon: Double, image: Option[String], neighboursIds: String, merchantsIds: String) extends InsertCommand {
  override def query: String = s"insert into ${CardsTable.NAME} values (?, ?, ?, ?, ?, ?, ?, ?, ?)"

  override def args: Array[AnyRef] = {
    Array(id.toString, number.toString, name, nameRaw, image.orNull, gpsLat.toString, gpsLon.toString, merchantsIds, neighboursIds)
  }
}

//TODO insert merchants command