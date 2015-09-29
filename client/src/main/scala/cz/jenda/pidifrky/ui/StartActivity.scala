package cz.jenda.pidifrky.ui

import java.lang.Thread.UncaughtExceptionHandler

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.afollestad.materialdialogs.MaterialDialog
import cz.jenda.pidifrky.{CardStatusTable, MerchantsTable, CardsTable, R}
import cz.jenda.pidifrky.data.Database
import cz.jenda.pidifrky.data.dao.{CardInsertCommand, MerchantInsertCommand}
import cz.jenda.pidifrky.logic.{DebugReporter, Toast}
import cz.jenda.pidifrky.ui.api.BasicActivity
import cz.jenda.pidifrky.ui.dialogs._

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

        System.exit(1)
      }
    })

    setContentView(R.layout.activity_start)
    findView(R.id.button, classOf[Button]).foreach(_.setText(R.string.app_author))
  }


  override protected def onStart(): Unit = {
    super.onStart()

    //        InfoDialog('testDialog, R.string.title_activity_gps_log, R.string.email_address).show()


  }


  override protected def onApplicationStart(): Unit = {
    super.onApplicationStart()

    //noinspection ScalaUselessExpression
    Database // init the database

    try {
      Database.truncate(CardsTable)
      Database.truncate(CardStatusTable)
      Database.truncate(MerchantsTable)

      Database.executeTransactionally(
        MerchantInsertCommand(1, "theMerchant", "themerchant", "addrs", 50, 14, preciseLocation = false),
        MerchantInsertCommand(2, "theMerchant2", "themerchant2", "addrs", 50.5, 14.5, preciseLocation = false),
        CardInsertCommand(1, 1, "theCard", "thecard", 50.3, 14.3, None, "", ""),
        CardInsertCommand(2, 2, "theCard2", "thecard2", 50.7, 14.7, None, "", "")
      )
    }
    catch {
      case e: Exception =>
        DebugReporter.debugAndReport(e)
    }


    //    val dialog = NormalProgressDialog('testDialog, R.string.menu_display, R.string.menu_display, 100, cancellable = false)
    //
    //    dialog.show()
    //
    //    runAsync {
    //      for (i <- 1 to 100) {
    //        Thread.sleep(200)
    //        dialog.setProgress(i)
    //      }
    //    }


    //    SingleChoiceDialog('testDialog, R.string.menu_display, R.array.filterTypes).show()

    //    runAsync {
    //      val http = new HttpRequester("https://google.com")
    //
    //      try {
    //        val r = http.execute()
    //
    //        DebugReporter.debug(r.asString())
    //      }
    //      catch {
    //        case e: Exception =>
    //          e.printStackTrace()
    //          DebugReporter.debug(e)
    //      }
    //    }

  }

  def click(v: View): Unit = {
    goTo(classOf[CardsListActivity])
  }

  def goToMap(v: View): Unit = {
    goTo(classOf[MapActivity])
  }

  def goToGpsLog(v: View): Unit = {
    goTo(classOf[GpsLogActivity])
  }

  override def onDialogResult(dialogId: Symbol, dialog: MaterialDialog, result: IndexDialogResult): Unit = {
    Toast("hurra " + result.index, Toast.Short)
  }
}
