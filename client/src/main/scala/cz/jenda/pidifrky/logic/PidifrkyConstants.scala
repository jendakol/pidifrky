package cz.jenda.pidifrky.logic

import com.google.android.gms.maps.model.LatLng
import cz.jenda.pidifrky.logic.map.LocationHelper

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object PidifrkyConstants {
  val DATABASE_VERSION = 4
  val DATABASE_NAME = "pidifrky.db"

  //port 9000 for testing in Genymotion; don't forget to overwrite hosts file on device
  //TODO https
  val URL_BASE = "http://pidifrky.jenda.eu:9000/device"
  val URL_DOWNLOAD_IMAGES = URL_BASE + "/downloadImages"
  val URL_ROUTE = "http://maps.googleapis.com/maps/api/directions/json"
  val URL_CARD_DETAIL = "http://pidifrk.cz/pidifrk-detail.php?pid=%d"
  val URL_MERCHANT_DETAIL = "http://pidifrk.cz/prodejce-pidifrku.php?idp=%d"

  val HEADER_PAYLOAD = "Pidifrky-Payload"

  val STORAGE_TYPE = "storageType"
  val PATH_BASE = "pidifrky"
  val PATH_IMAGES_FULL = PATH_BASE + "/images/full"
  val PATH_IMAGES_THUMBS = PATH_BASE + "/images/thumbs"
  val THUMBS_DOWNLOADED_PREFIX = "_thumb_"

  val DATABASE_TMP_FILENAME = "data_pidifrky.json"
  val BACKUP_DIR = "pidifrkyBackup"
  val BACKUP_OWNEDCARDS_FILE = "ownedCards.properties"
  val BACKUP_WANTEDCARDS_FILE = "wantedCards.properties"
  val BACKUP_IMAGES_DIR = "images"
  val STARTED_COUNT = "startedCount"
  val RATED = "hasRated"
  val DATABASE_LAST_UPDATE = "databaseLastUpdate"

  val PREF_DISTANCE_CLOSEST = "closest_distance"
  val PREF_REFRESH_LIST = "refresh_list"
  val PREF_REFRESH_DETAIL = "refresh_detail"
  val PREF_REFRESH_MAP = "refresh_map"
  val PREF_LAST_VERSION = "lastVersion"
  val PREF_TRACKING = "analyticsTracking"
  val PREF_GPS_OFF_TIMEOUT = "gps_off_timeout"
  val PREF_SHOW_TILES_NUMBERS = "show_tiles_numbers"
  val PREF_SHOW_TILES_FOUND = "show_tiles_found"
  val PREF_SHOW_CARDS_NUMBERS = "show_cards_numbers"
  val PREF_UUID = "uuid"
  val PREF_ACCOUNT_EMAIL = "accountEmail"
  val PREF_DEBUG_ALLOWED = "debugAllowed"
  val PREF_DEBUG_AUTOSEND = "debugAutoSend"

  val PREF_MAP_TYPE = "mapType"
  val PREF_MAP_FOLLOW_LOCATION = "mapFollowLocation"

  val MAP_CENTER = new LatLng(49.8401903, 15.3693800)
}