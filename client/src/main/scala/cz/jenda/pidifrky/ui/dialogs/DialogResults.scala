package cz.jenda.pidifrky.ui.dialogs

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
sealed trait DialogResult {
}

case class EmptyDialogResult() extends DialogResult

case class IndexDialogResult(index: Int) extends DialogResult