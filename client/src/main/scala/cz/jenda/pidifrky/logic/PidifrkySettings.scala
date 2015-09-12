package cz.jenda.pidifrky.logic

import android.content.{Context, SharedPreferences}
import android.preference.PreferenceManager
import com.splunk.mint.Mint
import cz.jenda.pidifrky.logic.map.MapType

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object PidifrkySettings {
  private var preferences: SharedPreferences = _

  def init(implicit context: Context): Unit = {
    preferences = PreferenceManager.getDefaultSharedPreferences(context)

    //when initializing
    Mint.setUserIdentifier(PidifrkySettings.UUID)
  }

  lazy val UUID: String = {
    if (!preferences.contains(PidifrkyConstants.PREF_UUID)) {
      val uuid: String = java.util.UUID.randomUUID.toString
      preferences.edit.putString(PidifrkyConstants.PREF_UUID, uuid).apply()
      uuid
    }
    else {
      preferences.getString(PidifrkyConstants.PREF_UUID, java.util.UUID.randomUUID.toString)
    }
  }

  def contact: Option[String] =
    if (preferences.contains(PidifrkyConstants.PREF_ACCOUNT_EMAIL))
      Option(preferences.getString(PidifrkyConstants.PREF_ACCOUNT_EMAIL, ""))
    else
      None

  def trackingEnabled: Boolean = readBoolean(PidifrkyConstants.PREF_TRACKING, default = false)

  def debugCollecting: Boolean = Utils.isDebug || readBoolean(PidifrkyConstants.PREF_DEBUG_ALLOWED, default = false)

  def debugAutoSend: Boolean = readBoolean(PidifrkyConstants.PREF_DEBUG_AUTOSEND, default = true)

  def gpsUpdateIntervals: GpsIntervalSettings = GpsIntervalSettings(
    readInt(PidifrkyConstants.PREF_REFRESH_LIST, 3000),
    readInt(PidifrkyConstants.PREF_REFRESH_DETAIL, 3000),
    readInt(PidifrkyConstants.PREF_REFRESH_MAP, 2000)
  )

  def followLocationOnMap: Boolean = readBoolean(PidifrkyConstants.PREF_MAP_FOLLOW_LOCATION, default = false)

  def mapType: MapType = MapType(readInt(PidifrkyConstants.PREF_MAP_TYPE, 1))

  def closestDistance: Int = readInt(PidifrkyConstants.PREF_DISTANCE_CLOSEST, 30000)

  def gpsTimeout: Int = readInt(PidifrkyConstants.PREF_GPS_OFF_TIMEOUT, 5000)

  def markAndGetStarts: Int = try {
    val started: Int = preferences.getInt(PidifrkyConstants.STARTED_COUNT, 0) + 1
    preferences.edit.putInt(PidifrkyConstants.STARTED_COUNT, started).apply()
    started
  }
  catch {
    case e: Exception =>
      DebugReporter.debug(e)
      1
  }

  def starts: Int = readInt(PidifrkyConstants.STARTED_COUNT, 0)

  def ratedApp: Boolean = readBoolean(PidifrkyConstants.RATED, default = false)

  def ratedApp(rated: Boolean): Unit = editor.putBoolean(PidifrkyConstants.RATED, rated).apply()

  def databaseHashes: DatabaseHashes =
    DatabaseHashes(preferences.getString(PidifrkyConstants.DATABASE_HASH_CARDS, "_"), preferences.getString(PidifrkyConstants.DATABASE_HASH_MERCHANTS, "_"))

  def editor: SharedPreferences.Editor = preferences.edit()

  protected def readInt(name: String, default: Int): Int = try {
    preferences.getInt(name, default)
  }
  catch {
    case e: Exception =>
      DebugReporter.debug(e)
      default
  }

  protected def readBoolean(name: String, default: Boolean): Boolean = try {
    preferences.getBoolean(name, default)
  }
  catch {
    case e: Exception =>
      DebugReporter.debug(e)
      default
  }

  def sharedPreferences: Option[SharedPreferences] = Option(preferences)
}

case class GpsIntervalSettings(list: Int, detail: Int, map: Int)

case class DatabaseHashes(cards: String, merchants: String)
