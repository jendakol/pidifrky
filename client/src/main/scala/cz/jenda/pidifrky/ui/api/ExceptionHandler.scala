package cz.jenda.pidifrky.ui.api

import java.lang.Thread.UncaughtExceptionHandler

import android.content.Intent
import android.os.Bundle
import cz.jenda.pidifrky.logic.{Application, DebugReporter, Utils}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait ExceptionHandler extends BasicActivity {
  override protected def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler {
      override def uncaughtException(thread: Thread, ex: Throwable): Unit = {
        DebugReporter.debugAndReport(ex match {
          case e: Exception => e
          case _ => new scala.Exception(ex)
        })

        Application.currentActivity.foreach { act =>
          val intent = new Intent(act, getClass)
          intent.putExtra("exception", Array[String](ex.getClass.getSimpleName, ex.getMessage))
          intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NO_HISTORY)

          //TODO show message after soft restart
          Utils.restartApp(intent)
        }
      }
    })
  }
}
