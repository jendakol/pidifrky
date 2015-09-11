package cz.jenda.pidifrky.ui.dialogs

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.Builder

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class SingleChoiceDialog extends DialogWithResult[IndexDialogResult] {

  final protected var items: Int = _

  def withItems(items: Int): this.type = {
    this.items = items
    this
  }

  override protected def save: Bundle = {
    val bundle = super.save
    bundle.putInt("items", items)
    bundle
  }

  override protected def restore(savedState: Option[Bundle]): Unit = {
    super.restore(savedState)

    savedState.foreach { bundle =>
      items = bundle.getInt("items")
    }
  }

  protected def createDialogBuilder: Builder = {
    new MaterialDialog.Builder(ctx)
      .items(items)
      .itemsCallback(new MaterialDialog.ListCallback() {
      override def onSelection(materialDialog: MaterialDialog, view: View, i: Int, charSequence: CharSequence): Unit = {
        dialogResultCallback.foreach(_.onDialogResult(dialogId, materialDialog, IndexDialogResult(i)))
      }
    })
  }
}

object SingleChoiceDialog {
  def apply(dialogId: Symbol, title: Int, items: Int)(implicit ctx: AppCompatActivity) =
    new SingleChoiceDialogWrapper(dialogId,
      new SingleChoiceDialog()
        .withActivity(ctx)
        .withDialogId(dialogId)
        .withCancellable(false)
        .withTitle(title)
        .withItems(items))
}
