package cz.jenda.pidifrky.ui.dialogs

import android.support.v4.app.FragmentActivity

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait DialogWithResult[R <: DialogResult] extends BaseDialog {
  protected var dialogResultCallback: Seq[DialogResultCallback[R]] = Seq()

  def withResultCallback(callback: DialogResultCallback[R]): this.type = {
    dialogResultCallback = dialogResultCallback :+ callback
    this
  }

  override def withActivity(ctx: FragmentActivity): this.type = {
    super.withActivity(ctx)

    ctx match {
      case x: DialogResultCallback[R] => dialogResultCallback = dialogResultCallback :+ x
      case _ =>
    }

    this
  }
}
