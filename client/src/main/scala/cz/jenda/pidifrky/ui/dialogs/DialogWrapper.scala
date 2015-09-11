package cz.jenda.pidifrky.ui.dialogs

import cz.jenda.pidifrky.logic.DebugReporter

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait DialogWrapper[D <: BaseDialog] {
  val dialogId: Symbol
  private[dialogs] var dialog: D

  DialogWrapper.registerWrapper(this)

  private[dialogs] def updateDialog(dialog: BaseDialog): Unit = dialog match {
    case d: D => this.dialog = d
    case _ => DebugReporter.debug("Requested dialog has different type")
  }

  def show(): Unit = dialog.show()

  def dismiss(): Unit = dialog.dismiss()
}

object DialogWrapper {
  private[dialogs] var dialogs: Map[Symbol, DialogWrapper[_ <: BaseDialog]] = Map()

  def registerWrapper[D <: BaseDialog](wrapper: DialogWrapper[D]): Unit = dialogs += wrapper.dialogId -> wrapper

  def updateDialog(dialog: BaseDialog): Unit = dialogs(dialog.getDialogId).updateDialog(dialog)

  def unregisterWrapper(dialogId: Symbol): Unit = dialogs -= dialogId
}

class NormalProgressDialogWrapper(override val dialogId: Symbol, override private[dialogs] var dialog: NormalProgressDialog) extends DialogWrapper[NormalProgressDialog] {

  def setMessage(msgId: Int): Unit = dialog.setMessage(msgId)

  def setMaxProgress(max: Int): Unit = dialog.setMaxProgress(max)

  def setProgress(current: Int): Unit = dialog.setProgress(current)
}

class SingleChoiceDialogWrapper(override val dialogId: Symbol, override private[dialogs] var dialog: SingleChoiceDialog) extends DialogWrapper[SingleChoiceDialog]

class DialogWithButtonsWrapper(override val dialogId: Symbol, override private[dialogs] var dialog: DialogWithButtons) extends DialogWrapper[DialogWithButtons]

