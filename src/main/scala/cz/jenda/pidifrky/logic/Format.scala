package cz.jenda.pidifrky.logic

import java.util.Locale

import android.location.Location

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object Format {
  def apply(location: Location): String = {
    //TODO: check if it has all properties
    s"%.4f N, %.4f E, acc %d, %d°, %.0f km/h".formatLocal(Locale.US, location.getLatitude, location.getLongitude, location.getAccuracy.toInt, location.getBearing.toInt, location.getSpeed * 3.6)
  }

  def apply(location: Location, distance: Float): String = {
    //TODO: check if it has all properties
    s"%.4f N, %.4f E, acc %d, dist %.4f, %d°, %.0f km/h".formatLocal(Locale.US, location.getLatitude, location.getLongitude, location.getAccuracy.toInt, distance, location.getBearing.toInt, location.getSpeed * 3.6)
  }
}
