package cz.jenda.pidifrky.ui.dialogs

import android.support.v7.app.AppCompatActivity
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.MaterialDialog.Builder
import cz.jenda.pidifrky.R

import scala.util.Random

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object InfoDialog {
  def apply(dialogId: Symbol, title: Int, message: Int, cancellable: Boolean = false)(implicit ctx: AppCompatActivity): DialogWithButtonsWrapper =
    new DialogWithButtonsWrapper(dialogId,
      new DialogWithButtons()
        .withActivity(ctx)
        .withDialogId(dialogId)
        .withCancellable(cancellable)
        .withPositiveButton(PositiveDialogButton(Random.nextInt(), R.string.ok))
        .withTitle(title)
        .withMessage(message))
}
