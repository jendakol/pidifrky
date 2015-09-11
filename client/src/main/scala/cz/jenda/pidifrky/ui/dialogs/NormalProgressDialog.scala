package cz.jenda.pidifrky.ui.dialogs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.Builder

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait ProgressDialog extends BaseDialog with DialogWithMessage {

  def setMessage(msgId: Int): Unit = getMaterialDialog.foreach(_.setContent(msgId))

}

class NormalProgressDialog extends ProgressDialog {
  final protected var max: Int = _

  def withMax(max: Int): this.type = {
    this.max = max
    this
  }

  override protected def save: Bundle = {
    val bundle = super.save

    getMaterialDialog.foreach { dialog =>
      bundle.putInt("max", dialog.getMaxProgress)
      bundle.putInt("current", dialog.getCurrentProgress)
      bundle.putString("currentMessage", dialog.getContentView.getText.toString)
    }

    bundle
  }

  override protected def restore(savedState: Option[Bundle]): Unit = {
    super.restore(savedState)

    savedState.foreach { bundle =>
      max = bundle.getInt("max")
    }
  }

  def setMaxProgress(max: Int): Unit = getMaterialDialog.foreach(d => d.setMaxProgress(max))

  def setProgress(current: Int): Unit = getMaterialDialog.foreach(d => d.setProgress(current))

  override protected def createDialogBuilder: Builder = {
    val builder = super.createDialogBuilder

    builder.progress(false, max, true)

    builder
  }

  override protected def afterCreateDialog(savedState: Option[Bundle], dialog: MaterialDialog): Unit = {
    super.afterCreateDialog(savedState, dialog)

    savedState.foreach { bundle =>
      dialog.setProgress(bundle.getInt("current"))
      Option(bundle.getString("currentMessage")).foreach(dialog.setContent)
    }

  }
}

object NormalProgressDialog {
  def apply(dialogId: Symbol, title: Int, message: Int, max: Int, cancellable: Boolean)(implicit ctx: AppCompatActivity): NormalProgressDialogWrapper =
    new NormalProgressDialogWrapper(dialogId, new NormalProgressDialog()
      .withActivity(ctx)
      .withDialogId(dialogId)
      .withCancellable(cancellable)
      .withTitle(title)
      .withMessage(message)
      .withMax(max))
}
