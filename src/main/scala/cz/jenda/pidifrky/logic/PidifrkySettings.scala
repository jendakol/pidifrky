package cz.jenda.pidifrky.logic

import android.content.{Context, SharedPreferences}
import android.preference.PreferenceManager

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object PidifrkySettings {
  private var preferences: SharedPreferences = _

  def init(context: Context) = {
    preferences = PreferenceManager.getDefaultSharedPreferences(context)
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
}
