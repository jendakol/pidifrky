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

  def init(context: Context): Unit = {
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

  def isTrackingEnabled: Boolean = true //TODO

  def gpsUpdateIntervals: GpsIntervalSettings = GpsIntervalSettings(3000, 3000, 2000) //TODO

  def followLocationOnMap: Boolean = true //TODO

  def mapType: MapType = MapType(preferences.getInt("mapType", 1))

  def editor: SharedPreferences.Editor = preferences.edit()
}

case class GpsIntervalSettings(list: Int, detail: Int, map: Int)
