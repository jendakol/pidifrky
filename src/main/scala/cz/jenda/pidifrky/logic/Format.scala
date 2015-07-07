package cz.jenda.pidifrky.logic

import java.util.Locale

import android.location.Location

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object Format {
  def apply(location: Location, decimals: Int = 2): String = {
    //TODO: check if it has all properties
    s"%.${decimals}f N, %.${decimals}f E, acc %d, %d°, %.0f km/h".formatLocal(Locale.US, location.getLatitude, location.getLongitude, location.getAccuracy.toInt, location.getBearing.toInt, location.getSpeed * 3.6)
  }
}
