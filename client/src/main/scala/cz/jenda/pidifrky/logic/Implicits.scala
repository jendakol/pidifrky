package cz.jenda.pidifrky.logic

import scala.util.{Failure, Success, Try}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object Implicits {

  implicit class TryToOption[T](val t: Try[T]) extends AnyVal {
    def toOptionWithLogging: Option[T] = t match {
      case Success(v) => Option(v)
      case Failure(e) =>
        DebugReporter.debugAndReport(e)
        None
    }
  }

}
