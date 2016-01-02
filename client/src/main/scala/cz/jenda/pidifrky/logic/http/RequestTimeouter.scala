package cz.jenda.pidifrky.logic.http

import java.util.concurrent._

import cz.jenda.pidifrky.logic.DebugReporter

import scala.util.control.NonFatal

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object RequestTimeouter {
  private val executor = Executors.newSingleThreadScheduledExecutor()
  private val requestsMap = new ConcurrentHashMap[String, ScheduledFuture[_]]()

  def requestSent(request: RequestId)(timeoutAction: => Unit)(implicit requestSettings: RequestSettings = RequestSettings()): Unit = {
    val timeoutMillis = requestSettings.requestTimeout.getOrElse(2000)

    DebugReporter.debug(s"Planning timeout action for $request")

    val f = executor.schedule(new Runnable {
      override def run(): Unit = try {
        remove(request.value)
        DebugReporter.debug(s"Timing out $request")
        timeoutAction
      } catch {
        case NonFatal(e) => DebugReporter.debug("Timeout action has thrown an exception; this is probably a BUG", e)
      }
    }, timeoutMillis, TimeUnit.MILLISECONDS)

    if (requestsMap.putIfAbsent(request.value, f) != null) {
      DebugReporter.debug(s"Detected probably repeated $request, timeout may not work as expected")
    }
  }

  def requestFinished(request: RequestId): Unit = {
    remove(request.value)
    DebugReporter.debug(s"Request $request finished, won't be timed out")
  }

  private def remove(request: String): Unit = {
    Option(requestsMap.remove(request)).foreach(_.cancel(false))
  }
}
