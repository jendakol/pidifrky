package cz.jenda.pidifrky.ui

import java.lang.Thread.UncaughtExceptionHandler

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.afollestad.materialdialogs.MaterialDialog
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic._
import cz.jenda.pidifrky.logic.dbimport.DbImporter
import cz.jenda.pidifrky.ui.api.{Toast, BasicActivity}
import cz.jenda.pidifrky.ui.dialogs._

import scala.util.{Failure, Success}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class StartActivity extends BasicActivity with DialogResultCallback[IndexDialogResult] {
  override protected val hasParentActivity = false

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler {
      override def uncaughtException(thread: Thread, ex: Throwable): Unit = {
        DebugReporter.debugAndReport(ex match {
          case e: Exception => e
          case _ => new scala.Exception(ex)
        })

        Application.currentActivity.foreach { act =>
          val intent = new Intent(act, classOf[StartActivity])
          intent.putExtra("exception", Array[String](ex.getClass.getSimpleName, ex.getMessage))
          intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NO_HISTORY)

          //TODO show message after soft restart
          Utils.restartApp(intent)
        }
      }
    })

    setContentView(R.layout.activity_start)
    findView(R.id.button, classOf[Button]).foreach(_.setText(R.string.app_author))
  }

  override protected def onStart(): Unit = {
    super.onStart()
  }

  override protected def onApplicationStart(): Unit = {
    super.onApplicationStart()
  }

  def click(v: View): Unit = {
    goTo(classOf[CardsListActivity])
  }

  def goToMap(v: View): Unit = {
    goTo(classOf[MapActivity])
  }

  def update(v: View): Unit = {
    val dialog = NormalProgressDialog('testing, R.string.downloading_database, R.string.processing_cards, 100, cancellable = false)

    val progressListener = ProgressListener.forDialog(dialog)

    dialog.show()

    DbImporter.update(progressListener).andThen {
      case Success(_) =>
        dialog.dismiss()
        Toast("DB updated!", 3000)
      case Failure(e) =>
        Toast(Format(e), 3000)
        DebugReporter.debug(e)
        dialog.dismiss()
    }
  }

  def downloadImages(v: View): Unit = {
    DbImporter.downloadImages(full = true)
  }

  def goToGpsLog(v: View): Unit = {
    goTo(classOf[GpsLogActivity])
  }

  override def onDialogResult(dialogId: Symbol, dialog: MaterialDialog, result: IndexDialogResult): Unit = {
  }
}
