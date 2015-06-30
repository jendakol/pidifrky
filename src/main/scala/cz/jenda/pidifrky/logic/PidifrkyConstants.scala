package cz.jenda.pidifrky.logic

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object PidifrkyConstants {
  val MINT_API_KEY = "ec579bce"

  val BASE_URL: String = "https://pidifrky.jenda.eu"
  val URL_DATABASE: String = BASE_URL + "/getupdate.php"
  val URL_REPORT: String = BASE_URL + "/report.php"
  val URL_ROUTE: String = "http://maps.googleapis.com/maps/api/directions/json"
  val URL_CARD_DETAIL: String = "http://pidifrk.cz/pidifrk-detail.php?pid=%d"
  val URL_MERCHANT_DETAIL: String = "http://pidifrk.cz/prodejce-pidifrku.php?idp=%d"
  val DATABASE_TMP_FILENAME: String = "data_pidifrky.json"
  val DATABASE_NAME: String = "pidifrky.db"
  val BACKUP_DIR: String = "pidifrkyBackup"
  val BACKUP_OWNEDCARDS_FILE: String = "ownedCards.properties"
  val BACKUP_WANTEDCARDS_FILE: String = "wantedCards.properties"
  val BACKUP_IMAGES_DIR: String = "images"
  val DATABASE_VERSION: Int = 8
  val STARTED_COUNT: String = "startedCount"
  val RATED: String = "hasRated"
  val DATABASE_HASH_CARDS: String = "databaseCardsHash"
  val DATABASE_HASH_MERCHANTS: String = "databaseMerchantsHash"
  val PREF_DISTANCE_CLOSEST: String = "closest_distance"
  val PREF_REFRESH_LIST: String = "refresh_list"
  val PREF_REFRESH_DETAIL: String = "refresh_detail"
  val PREF_REFRESH_MAP: String = "refresh_map"
  val PREF_LAST_VERSION: String = "lastVersion"
  val PREF_TRACKING: String = "analyticsTracking"
  val PREF_GPS_OFF_TIMEOUT: String = "gps_off_timeout"
  val PREF_SHOW_TILES_NUMBERS: String = "show_tiles_numbers"
  val PREF_SHOW_TILES_FOUND: String = "show_tiles_found"
  val PREF_SHOW_CARDS_NUMBERS: String = "show_cards_numbers"
  val PREF_UUID: String = "uuid"
  val PREF_ACCOUNT_EMAIL: String = "accountEmail"
  val PREF_DEBUG_ALLOWED: String = "debugAllowed"
  val PREF_DEBUG_AUTOSEND: String = "debugAutoSend"
}