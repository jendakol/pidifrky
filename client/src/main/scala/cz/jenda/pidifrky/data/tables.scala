package cz.jenda.pidifrky.data

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
sealed trait EntityTable {
  def NAME: String

  def getColumns: Array[String]
}

object CardsTable extends EntityTable {
  val NAME = "cards"
  val COL_ID = "id"
  val COL_NUMBER = "number"
  val COL_NAME = "name"
  val COL_NAME_RAW = "name_raw"
  val COL_IMAGE = "image"
  val COL_GPS_LAT = "gps_lat"
  val COL_GPS_LON = "gps_lon"
  val COL_MERCHANTS_IDS = "merchants"
  val COL_NEIGHBOURS = "neighbours"

  override def getColumns: Array[String] = Array(COL_ID, COL_NUMBER, COL_NAME, COL_NAME_RAW, COL_IMAGE, COL_GPS_LAT, COL_GPS_LON, COL_MERCHANTS_IDS, COL_NEIGHBOURS)
}

object CardStatusTable extends EntityTable {
  val NAME = "card_status"
  val COL_CARD_ID = "card_id"
  val COL_TYPE = "type"
  val COL_ADDED = "added"
  val COL_REMOVED = "removed"

  override def getColumns: Array[String] = Array(COL_CARD_ID, COL_TYPE, COL_ADDED, COL_REMOVED)
}

object MerchantsTable extends EntityTable {

  val NAME = "merchants"
  val COL_ID = "id"
  val COL_NAME = "name"
  val COL_NAME_RAW = "name_raw"
  val COL_ADDRESS = "address"
  val COL_GPS_LAT = "gps_lat"
  val COL_GPS_LON = "gps_lon"
  val COL_GPS_PRECISE = "gps_precise"
  val COL_CARDS_IDS = "cards"

  override def getColumns: Array[String] = Array(COL_ID, COL_NAME, COL_NAME_RAW, COL_ADDRESS, COL_GPS_LAT, COL_GPS_LON, COL_GPS_PRECISE, COL_CARDS_IDS)
}

