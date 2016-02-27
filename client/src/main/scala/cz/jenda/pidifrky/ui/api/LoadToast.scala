package cz.jenda.pidifrky.ui.api

import android.graphics.Color
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.{DebugReporter, Utils}
import net.steamcrafted.loadtoast.{LoadToast => ToastLib}

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object LoadToast {

  import cz.jenda.pidifrky.logic.Application._

  def apply(text: String, f: Future[_])(implicit ctx: BasicActivity): Unit = Utils.runOnUiThread {
    try {
      val t = create(text)

      DebugReporter.debug(s"Showing loadToast '$text'")

      f.andThen {
        case Success(_) => Utils.runOnUiThread(t.success())
        case Failure(_) => Utils.runOnUiThread(t.error())
      }
    }
    catch {
      case NonFatal(e) =>
        DebugReporter.debug(e, "Error while creating LoadToast")
    }
  }

  def apply(textId: Int, f: Future[_])(implicit ctx: BasicActivity): Unit = {
    apply(ctx.getString(textId), f)
  }

  def withLoadToast[A](text: String)(f: => Future[A])(implicit ctx: BasicActivity): Future[A] = {
    apply(text, f)
    f
  }

  def withLoadToast[A](textId: Int)(f: => Future[A])(implicit ctx: BasicActivity): Future[A] = {
    withLoadToast(ctx.getString(textId))(f)
  }

  private def create(text: String)(implicit ctx: BasicActivity): ToastLib = {
    new ToastLib(ctx)
      .setText(text)
      .setTextColor(Color.WHITE)
      .setBackgroundColor(R.color.colorPrimary)
      .setProgressColor(Color.WHITE)
      .setTranslationY(200)
      .show()
  }
}
