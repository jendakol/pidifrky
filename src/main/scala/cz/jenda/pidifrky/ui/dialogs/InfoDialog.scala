package cz.jenda.pidifrky.ui.dialogs

import android.support.v7.app.AppCompatActivity
import cz.jenda.pidifrky.R

import scala.util.Random

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object InfoDialog {
  def apply(dialogId: Symbol, title: Int, message: Int, cancellable: Boolean)(implicit ctx: AppCompatActivity) =
    new DialogWithButtonsWrapper(dialogId,
      new DialogWithButtons()
        .withActivity(ctx)
        .withDialogId(dialogId)
        .withCancellable(cancellable)
        .withPositiveButton(PositiveDialogButton(Random.nextInt(), R.string.ok))
        .withTitle(title)
        .withMessage(message))
}