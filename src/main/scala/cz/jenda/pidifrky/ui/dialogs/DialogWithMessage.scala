package cz.jenda.pidifrky.ui.dialogs

import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.Builder

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait DialogWithMessage extends BaseDialog {
  final protected var message: Int = _

  def withMessage(msg: Int): this.type = {
    this.message = msg
    this
  }

  override protected def save: Bundle = {
    val bundle = super.save
    bundle.putInt("message", message)
    bundle
  }

  override protected def restore(savedState: Option[Bundle]): Unit = {
    super.restore(savedState)

    savedState.foreach { bundle =>
      message = bundle.getInt("message")
    }
  }

  protected def createDialogBuilder: Builder =
    new MaterialDialog.Builder(ctx)
      .content(message)
}
