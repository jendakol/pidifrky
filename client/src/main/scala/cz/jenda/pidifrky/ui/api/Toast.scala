package cz.jenda.pidifrky.ui.api

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.widget.{TextView, Toast => AndroidToast}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.{DebugReporter, Utils}

import scala.util.control.NonFatal

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object Toast {
  final val Short = AndroidToast.LENGTH_SHORT
  final val Long = AndroidToast.LENGTH_LONG

  private var toast: Option[AndroidToast] = None

  def apply(text: String, duration: Int)(implicit ctx: Activity): Unit = Toast.synchronized {
    dismiss()

    Utils.runOnUiThread {
      try {
        DebugReporter.debug(s"Showing toast '$text'")

        val toast = new AndroidToast(ctx)
        toast.setDuration(AndroidToast.LENGTH_LONG)

        toast.setView {
          val view = new TextView(ctx)
          view.setText(text)
          view.setTextColor(Color.WHITE)
          view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16)
          view.setBackgroundResource(R.drawable.toastshape)

          view
        }

        toast.show()
        this.toast = Some(toast)
      }
      catch {
        case NonFatal(e) => DebugReporter.debug(e, "Error while showing the Toast")
      }
    }
  }

  def apply(textId: Int, duration: Int)(implicit ctx: Activity): Unit = apply(ctx.getString(textId), duration)

  //TODO undo button?

  //  def apply(text: String, button: ToastButton, duration: Int)(implicit ctx: Activity): Unit = Toast.synchronized {
  //    dismiss()

  //    val toast = createSuperToast(text, duration, SuperToast.Type.BUTTON)
  //    toast.setButtonIcon(button.icon, button.text)
  //
  //    val onClickWrapper = new OnClickWrapper(System.currentTimeMillis() + "_" + Random.nextFloat(), new OnClickListener {
  //      override def onClick(view: View, parcelable: Parcelable): Unit = button.onClick.apply(())
  //    })
  //
  //    toast.setOnClickWrapper(onClickWrapper)
  //
  //    toast.show()
  //    this.toast = Some(ToastAndWrapper(toast, Some(onClickWrapper)))
  //  }

  /* ---- */

  def dismiss(): Unit = Toast.synchronized {
    toast.foreach(toast => try {
      toast.cancel()
    }
    catch {
      case NonFatal(e) => DebugReporter.debug(e, "Error while dismissing the toast")
    })
  }

  def onSaveState(outState: Bundle): Unit = {
  }

  def onRestoreState(savedState: Bundle)(implicit ctx: Activity): Unit = {
  }
}
