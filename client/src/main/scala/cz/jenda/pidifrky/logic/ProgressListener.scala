package cz.jenda.pidifrky.logic

import cz.jenda.pidifrky.ui.dialogs.NormalProgressDialogWrapper

/**
 * @author Jenda Kolena, kolena@avast.com
 */
trait ProgressListener {
  def apply(percent: Int): Unit
}

object ProgressListener {
  def noop(): ProgressListener = new ProgressListener {
    override def apply(percent: Int): Unit = ()
  }

  def forDialog(dialog: NormalProgressDialogWrapper): ProgressListener = new ProgressListener {
    dialog.setMaxProgress(100)

    override def apply(percent: Int): Unit = dialog.setProgress(percent)
  }
}
