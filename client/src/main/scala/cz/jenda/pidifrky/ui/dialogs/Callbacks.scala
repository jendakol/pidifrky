package cz.jenda.pidifrky.ui.dialogs

import com.afollestad.materialdialogs.MaterialDialog

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait DialogConfirmedCallback {
  def onDialogConfirmed(dialogId: Symbol, dialog: MaterialDialog, button: DialogButton): Unit
}

trait DialogResultCallback[R <: DialogResult] {
  def onDialogResult(dialogId: Symbol, dialog: MaterialDialog, result: R): Unit
}

trait DialogCancelledCallback {
  def onDialogCancelled(dialogId: Symbol): Unit
}