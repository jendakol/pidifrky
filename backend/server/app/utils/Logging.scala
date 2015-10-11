package utils

/**
 * @author Jenda Kolena, kolena@avast.com
 */
trait Logging {
  protected implicit val Logger = play.api.Logger(getClass)
}
