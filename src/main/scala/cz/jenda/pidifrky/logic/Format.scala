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
    s"%.4fN, %.4fE, acc %d, dist %.1f, %.0f km/h".formatLocal(Locale.US, location.getLatitude, location.getLongitude, location.getAccuracy.toInt, distance, location.getSpeed * 3.6)
  }

  def apply(t: Throwable): String = t.getClass.getSimpleName + ": " + t.getMessage


  /**
   * Formats file size according to given locale.
   *
   * @param bytes  Bytes to be formatted.
   * @param locale The locale.
   * @return Formatted size.
   * @see #formatSize(long)
   */
  def formatSize(bytes: Long, locale: Locale = Locale.US): String = {
    val unit: Int = 1024
    if (bytes < unit) return bytes + " B"
    val exp: Int = (Math.log(bytes) / Math.log(unit)).toInt
    "%.2f %sB".formatLocal(locale, bytes / Math.pow(unit, exp), "kMGTPE".charAt(exp - 1))
  }

  /**
   * Formats file size according to given locale.
   *
   * @param speed  Speed to be formatted.
   * @param locale The locale.
   * @return Formatted speed.
   * @see #formatSpeed(double)
   */
  def formatSpeed(speed: Double, locale: Locale = Locale.US): String = {
    val unit: Int = 1024
    if (speed < unit) return speed + " B/s"
    val exp: Int = (Math.log(speed) / Math.log(unit)).toInt
    "%.2f %sB/s".formatLocal(locale, speed / Math.pow(unit, exp), "kMGTPE".charAt(exp - 1))
  }

  /**
   * Formats percents according to given locale.
   *
   * @param percents Percents to be formatted.
   * @param locale   The locale.
   * @return Formatted percents.
   * @see #formatPercent(double)
   */
  def formatPercent(percents: Double, locale: Locale = Locale.US): String = "%.2g%".formatLocal(locale, percents)
}
