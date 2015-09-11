package cz.jenda.pidifrky.logic

import android.app.Activity
import android.graphics.Color
import android.os.{Bundle, Parcelable}
import android.view.View
import com.github.johnpersano.supertoasts.SuperToast.OnClickListener
import com.github.johnpersano.supertoasts.util.{OnClickWrapper, Wrappers}
import com.github.johnpersano.supertoasts.{SuperActivityToast, SuperToast}
import cz.jenda.pidifrky.R

import scala.util.Random

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object Toast {
  final val Short = 1500
  final val Medium = 2500
  final val Long = 4000

  private var toast: Option[ToastAndWrapper[_]] = None

  def apply(text: String, duration: Int)(implicit ctx: Activity): Unit = Toast.synchronized {
    dismiss()

    val toast = createSimpleToast(text, duration)

    Utils.runOnUiThread {
      toast.show()
    }

    this.toast = Some(ToastAndWrapper(toast))
  }

  def apply(textId: Int, duration: Int)(implicit ctx: Activity): Unit = apply(ctx.getString(textId), duration)

  def apply(text: String, button: ToastButton, duration: Int)(implicit ctx: Activity): Unit = Toast.synchronized {
    dismiss()

    val toast = createSuperToast(text, duration, SuperToast.Type.BUTTON)
    toast.setButtonIcon(button.icon, button.text)

    val onClickWrapper = new OnClickWrapper(System.currentTimeMillis() + "_" + Random.nextFloat(), new OnClickListener {
      override def onClick(view: View, parcelable: Parcelable): Unit = button.onClick.apply(())
    })

    toast.setOnClickWrapper(onClickWrapper)

    toast.show()
    this.toast = Some(ToastAndWrapper(toast, Some(onClickWrapper)))
  }

  def apply(textId: Int, button: ToastButton, duration: Int)(implicit ctx: Activity): Unit =
    apply(ctx.getString(textId), button, duration)

  /* ---- */

  protected def createSuperToast(text: String, duration: Int, toastType: SuperToast.Type)(implicit ctx: Activity): SuperActivityToast = {
    val toast = new SuperActivityToast(ctx, toastType)
    toast.setDuration(duration)
    toast.setTouchToDismiss(true)
    toast.setText(text)
    toast.setBackground(SuperToast.Background.BLACK)
    toast.setTextColor(Color.WHITE)
    toast
  }

  protected def createSimpleToast(text: String, duration: Int)(implicit ctx: Activity): SuperToast = {
    val toast = new SuperToast(ctx)
    toast.setDuration(duration)
    toast.setText(text)
    toast.setBackground(SuperToast.Background.BLACK)
    toast.setTextColor(Color.WHITE)
    toast
  }

  def dismiss(): Unit = {
    SuperActivityToast.cancelAllSuperActivityToasts()
    toast.foreach(toast => try {
      toast.dismiss()
    }
    catch {
      case e: Exception => DebugReporter.debug(e, "Error while dismissing the toast")
    })
  }

  def onSaveState(outState: Bundle): Unit = SuperActivityToast.onSaveState(outState)

  def onRestoreState(savedState: Bundle)(implicit ctx: Activity): Unit = {
    val wrappers = new Wrappers
    toast.foreach(toast => toast.onClickWrapper.foreach(wrappers.add))
    SuperActivityToast.onRestoreState(savedState, ctx, wrappers)
  }
}

case class ToastAndWrapper[T](someToast: T, onClickWrapper: Option[OnClickWrapper] = None)(implicit ev: (SuperActivityToast with SuperToast) <:< T) {
  def dismiss(): Unit = someToast match {
    case t: SuperActivityToast => if (t.isShowing) t.dismiss()
    case t: SuperToast => if (t.isShowing) t.dismiss()
  }

  def isShowing: Boolean = someToast match {
    case t: SuperActivityToast => t.isShowing
    case t: SuperToast => t.isShowing
  }
}

trait ToastButton {
  val icon: Int
  val text: String
  val onClick: Unit => Unit
}

object ToastButton {
  def UNDO(listener: => Unit)(implicit ctx: Activity): ToastButton = new ToastButton {
    override val text: String = ctx.getString(R.string.cancel)
    override val onClick = (_: Unit) => listener
    override val icon: Int = SuperToast.Icon.Dark.UNDO
  }
}
