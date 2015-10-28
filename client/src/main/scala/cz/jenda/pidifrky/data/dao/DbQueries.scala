package cz.jenda.pidifrky.data.dao

import cz.jenda.pidifrky.data.{CardStatusTable, CardsTable, MerchantsTable}

/**
 * @author Jenda Kolena, kolena@avast.com
 */
object DbQueries {
  val getCards = s"select ${CardsTable.getColumns.mkString(", ")}, " +
    s"(select ${CardStatusTable.COL_TYPE} from ${CardStatusTable.NAME} where ${CardStatusTable.NAME}.${CardStatusTable.COL_CARD_ID} = ${CardsTable.NAME}.${CardsTable.COL_ID})" +
    "from " + CardsTable.NAME

  def getCards(ids: Int*): String = {
    val q = if (ids.nonEmpty) ids.map(i => "id = " + i).mkString(" where ", " OR ", "") else ""

    getCards + q
  }

  val getAllCardsIds = s"select id from ${CardsTable.NAME}"

  def getNearestCards(latMin: Double, latMax: Double, lonMin: Double, lonMax: Double): String = getCards +
    s" where (${CardsTable.COL_GPS_LAT} between $latMin and $latMax) " +
    s"and (${CardsTable.COL_GPS_LON} between $lonMin and $lonMax)"

  /* ----- ----- ----- */

  def getMerchants(ids: Int*): String = {
    val q = if (ids.nonEmpty) ids.map(i => "id = " + i).mkString(" where ", " OR ", "") else ""

    s"select ${MerchantsTable.getColumns.mkString(", ")} from ${MerchantsTable.NAME} $q"
  }

  def getNearestMerchants(latMin: Double, latMax: Double, lonMin: Double, lonMax: Double): String = s"select ${MerchantsTable.getColumns.mkString(", ")} from ${MerchantsTable.NAME}" +
    s" where (${MerchantsTable.COL_GPS_LAT} between $latMin and $latMax) " +
    s"and (${MerchantsTable.COL_GPS_LON} between $lonMin and $lonMax)"
}
