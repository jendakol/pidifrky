package cz.jenda.pidifrky.logic

import com.splunk.mint.Mint

import scala.util.{Failure, Success, Try}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object Transaction {
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
}
