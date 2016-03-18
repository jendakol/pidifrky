package cz.jenda.pidifrky.logic

import java.util

import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.preference.PreferenceManager
import com.splunk.mint.Mint
import com.sromku.simple.storage.SimpleStorage.StorageType
import cz.jenda.pidifrky.logic.map.MapType

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object PidifrkySettings {
  private lazy val preferences: SharedPreferences = Application.appContext.map { context =>
    PreferenceManager.getDefaultSharedPreferences(context)
  }.getOrElse({
    DebugReporter.debugAndReport(new Exception("Missing context for settings"))
    null
  })

  lazy val UUID: String = {
    if (!preferences.contains(PidifrkyConstants.PREF_UUID)) {
      val uuid: String = java.util.UUID.randomUUID.toString
      preferences.edit.putString(PidifrkyConstants.PREF_UUID, uuid).apply()
      Mint.setUserIdentifier(uuid)
      uuid
    }
    else {
      val uuid = preferences.getString(PidifrkyConstants.PREF_UUID, java.util.UUID.randomUUID.toString)
      Mint.setUserIdentifier(uuid)
      uuid
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
    readInt(PidifrkyConstants.PREF_REFRESH_MAP, 3000)
  )

  def followLocationOnMap: Boolean = readBoolean(PidifrkyConstants.PREF_MAP_FOLLOW_LOCATION, default = false)

  def mapType: MapType = MapType(readInt(PidifrkyConstants.PREF_MAP_TYPE, 1))

  def closestDistance: Int = readInt(PidifrkyConstants.PREF_DISTANCE_CLOSEST, 20000)

  def gpsTimeout: Int = readInt(PidifrkyConstants.PREF_GPS_OFF_TIMEOUT, 5000)

  def markAndGetStarts: Int = try {
    val started: Int = readInt(PidifrkyConstants.STARTED_COUNT, 0) + 1
    withEditor(_.putInt(PidifrkyConstants.STARTED_COUNT, started))
    started
  }
  catch {
    case e: Exception =>
      DebugReporter.debug(e)
      1
  }

  def starts: Int = readInt(PidifrkyConstants.STARTED_COUNT, 0)

  def ratedApp: Boolean = readBoolean(PidifrkyConstants.RATED, default = false)

  def ratedApp(rated: Boolean): Unit = withEditor(_.putBoolean(PidifrkyConstants.RATED, rated))

  def lastDatabaseUpdateTimestamp: Long = readLong(PidifrkyConstants.DATABASE_LAST_UPDATE, 0)

  def markDatabaseUpdate(): Unit = withEditor(_.putLong(PidifrkyConstants.DATABASE_LAST_UPDATE, System.currentTimeMillis()))

  def storageType: Option[StorageType] = {
    val i = readInt(PidifrkyConstants.STORAGE_TYPE, -1)

    (if (i >= 0) Some(i) else None).map(StorageType.values())
  }

  def setStorageType(storageType: StorageType): Unit =
    withEditor(_.putInt(PidifrkyConstants.STORAGE_TYPE, storageType.ordinal()))

  /* ----- ----- ----- ----- ----- */

  def showCardsNumbers: Boolean = readBoolean(PidifrkyConstants.PREF_SHOW_CARDS_NUMBERS, default = false)

  def showTilesNumbers: Boolean = readBoolean(PidifrkyConstants.PREF_SHOW_TILES_NUMBERS, default = false)

  def showTilesFound: Boolean = readBoolean(PidifrkyConstants.PREF_SHOW_TILES_FOUND, default = false)

  /* ----- ----- ----- ----- ----- */

  def withEditor[T](f: PreferencesEditor => T): T = {
    val editor = preferences.edit()
    val result = f(new PreferencesEditor(editor))
    editor.apply()
    result
  }

  protected def readInt(name: String, default: Int): Int = try {
    preferences.getString(name, default.toString).toInt
  }
  catch {
    case e: Exception =>
      DebugReporter.debug(e)
      default
  }

  protected def readLong(name: String, default: Long): Long = try {
    preferences.getString(name, default.toString).toLong
  }
  catch {
    case e: Exception =>
      DebugReporter.debug(e)
      default
  }

  protected def readBoolean(name: String, default: Boolean): Boolean = try {
    preferences.getBoolean(name, default)
    //    preferences.getString(name, default.toString).toBoolean
  }
  catch {
    case e: NullPointerException =>
      //some weird bug, trying to avoid stack overflow
      default
    case e: Exception =>
      DebugReporter.debug(e)
      default
  }

  def sharedPreferences: Option[SharedPreferences] = Option(preferences)
}

class PreferencesEditor(wrapped: Editor) extends Editor {
  override def putString(key: String, value: String): Editor = wrapped.putString(key, value)

  override def clear(): Editor = wrapped.clear()

  override def putFloat(key: String, value: Float): Editor = wrapped.putString(key, value.toString)

  override def remove(key: String): Editor = wrapped.remove(key)

  override def putBoolean(key: String, value: Boolean): Editor = wrapped.putBoolean(key, value)

  //  override def putBoolean(key: String, value: Boolean): Editor = wrapped.putString(key, value.toString)

  override def putInt(key: String, value: Int): Editor = wrapped.putString(key, value.toString)

  override def apply(): Unit = wrapped.apply()

  override def putLong(key: String, value: Long): Editor = wrapped.putString(key, value.toString)

  override def putStringSet(key: String, values: util.Set[String]): Editor = wrapped.putStringSet(key, values)

  override def commit(): Boolean = wrapped.commit()
}

case class GpsIntervalSettings(list: Int, detail: Int, map: Int)
