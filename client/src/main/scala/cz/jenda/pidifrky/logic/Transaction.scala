package cz.jenda.pidifrky.logic

import com.splunk.mint.Mint

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object Transaction {
  import Application._

  def apply[T](name: String)(block: => T): Try[T] = {
    Mint.transactionStart(name)
    try {
      val r = block
      Mint.transactionStop(name, "result", r.toString)
      Success(r)
    }
    catch {
      case e: Exception =>
        Mint.transactionCancel(name, e.getClass.getName + ": " + e.getMessage)
        DebugReporter.debugAndReport(e, s"Error while executing transaction '$name'")
        Failure(e)
    }
  }

  def async[T](name: String)(block: => Future[T]): Future[T] = {
    Mint.transactionStart(name)

    block.andThen {
      case Success(r) =>
        Mint.transactionStop(name, "result", r.toString)
      case Failure(e) =>
        Mint.transactionCancel(name, e.getClass.getName + ": " + e.getMessage)
        DebugReporter.debugAndReport(e, s"Error while executing transaction '$name'")
    }
  }
}
