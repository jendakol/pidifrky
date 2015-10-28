package utils

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait Logging {
  protected implicit val Logger = play.api.Logger(getClass)
}
