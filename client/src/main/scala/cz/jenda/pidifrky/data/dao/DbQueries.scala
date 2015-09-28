package cz.jenda.pidifrky.data.dao

import cz.jenda.pidifrky.{CardStatusTable, CardsTable}

/**
 * @author Jenda Kolena, kolena@avast.com
 */
object DbQueries {
  val getCards = s"select ${CardsTable.getColumns.mkString(", ")}, " +
    s"(select ${CardStatusTable.COL_TYPE} from ${CardStatusTable.NAME} where ${CardStatusTable.NAME}.${CardStatusTable.COL_CARD_ID} = ${CardsTable.NAME}.${CardsTable.COL_ID})" +
    "from " + CardsTable.NAME

  def getNearestCards(latMin: Double, latMax: Double, lonMin: Double, lonMax: Double): String = getCards +
    s" where (${CardsTable.COL_GPS_LAT} between $latMin and $latMax) " +
    s"and (${CardsTable.COL_GPS_LON} between $lonMin and $lonMax)"
}
