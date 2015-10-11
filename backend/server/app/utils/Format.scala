package utils

import java.util.Locale

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object Format {

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
