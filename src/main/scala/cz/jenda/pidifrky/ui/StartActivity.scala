package cz.jenda.pidifrky.ui

import java.lang.Thread.UncaughtExceptionHandler

import android.os.Bundle
import android.view.View
import android.widget.Button
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.{DebugReporter, Utils}
import cz.jenda.pidifrky.ui.api.BasicActivity

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class StartActivity extends BasicActivity {
  override protected val hasParentActivity = false

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler {
      override def uncaughtException(thread: Thread, ex: Throwable): Unit = {
        DebugReporter.debugAndReport(ex match {
          case e: Exception => e
          case _ => new scala.Exception(ex)
        })

        Utils.toast(ex.getMessage)
      }
    })

    setContentView(R.layout.main)
    findView(R.id.button, classOf[Button]).foreach(_.setText(R.string.hello_world))
  }

  def click(v: View): Unit = {
    goTo(classOf[CardsListActivity])
  }
}
