package cz.jenda.pidifrky.logic.location

import cz.jenda.pidifrky.logic.{Application, DebugReporter, Utils}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object GpsLogger {
  type EventListener = String => Unit

  private var listener: Option[EventListener] = None

  def setListener(listener: EventListener): Unit = this.listener = Some(listener)

  def addEvent(event: String): Unit = {
    DebugReporter.debug(event)
    Application.currentActivity.foreach { implicit ctx =>
      Utils.runOnUiThread(listener.foreach(_.apply(event)))
    }
  }
}
