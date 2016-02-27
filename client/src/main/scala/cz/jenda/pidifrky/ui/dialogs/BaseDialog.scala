package cz.jenda.pidifrky.ui.dialogs

import android.app.{Activity, Dialog}
import android.content.DialogInterface
import android.content.DialogInterface.OnCancelListener
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.{DialogFragment, FragmentActivity}
import android.support.v7.app.AppCompatActivity
import com.afollestad.materialdialogs.MaterialDialog
import cz.jenda.pidifrky.logic.DebugReporter

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait BaseDialog extends DialogFragment {
  final protected implicit var ctx: FragmentActivity = _

  final protected var dialogId: Symbol = _

  final protected var cancellable: Boolean = _

  final protected var title: Int = _

  final protected var dialogCancelledCallback: Option[DialogCancelledCallback] = None


  def withActivity(ctx: FragmentActivity): this.type = {
    this.ctx = ctx

    ctx match {
      case x: DialogCancelledCallback => dialogCancelledCallback = Some(x)
      case _ =>
    }

    this
  }

  def withDialogId(dialogId: Symbol): this.type = {
    this.dialogId = dialogId
    this
  }

  def withCancellable(cancellable: Boolean): this.type = {
    this.cancellable = cancellable
    this
  }

  def withTitle(title: Int): this.type = {
    this.title = title
    this
  }

  protected def createDialogBuilder: MaterialDialog.Builder

  protected def restore(savedState: Option[Bundle]): Unit = savedState.foreach { bundle =>
    title = bundle.getInt("title")
    if (title == 0) DebugReporter.debug("Cannot load dialog title from bundle")

    val id = bundle.getString("dialogId")
    if (id == null) {
      DebugReporter.debug("Cannot load dialogId from bundle")
    }
    else {
      dialogId = Symbol(id)
    }

    cancellable = bundle.getBoolean("cancellable", true)
  }

  protected def save: Bundle = {
    val bundle = new Bundle()

    bundle.putInt("title", title)
    bundle.putBoolean("cancellable", cancellable)
    bundle.putString("dialogId", dialogId.name)

    bundle
  }


  protected def getMaterialDialog: Option[MaterialDialog] = super.getDialog match {
    case m: MaterialDialog => Some(m)
    case _ =>
      DebugReporter.breadcrumb("Dialog is null!")
      None
  }

  protected def afterCreateDialog(savedState: Option[Bundle], dialog: MaterialDialog): Unit = {}

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    super.onCreateDialog(savedInstanceState)

    val bundle = Option(savedInstanceState)

    restore(bundle)

    val builder = createDialogBuilder

    builder
      .itemsColor(Color.WHITE)
      .negativeColor(Color.WHITE)
      .neutralColor(Color.WHITE)
      .positiveColor(Color.WHITE)


    builder.cancelListener(new OnCancelListener {
      override def onCancel(dialog: DialogInterface): Unit = try {
        dialogCancelledCallback.foreach(d => d.onDialogCancelled(dialogId))
      }
      catch {
        case e: Exception => DebugReporter.debugAndReport(e, "Error while executing cancel callback")
      }
    })

    builder
      .title(title)
      .cancelable(cancellable)


    DebugReporter.debug(s"Creating dialog '${dialogId.name}'")

    val dialog = builder.build()

    afterCreateDialog(bundle, dialog)

    setCancelable(cancellable)

    DialogWrapper.updateDialog(this)

    dialog
  }


  override def onAttach(activity: Activity): Unit = {
    super.onAttach(activity)
    activity match {
      case a: AppCompatActivity => withActivity(a)
      case a: FragmentActivity =>
        DebugReporter.debug(s"Attached activity is not AppCompatActivity, is $activity")
        withActivity(a)
      case _ => DebugReporter.breadcrumb(s"Attached activity is $activity")
    }
  }

  def show(): Unit = try {
    DebugReporter.debug(s"Showing dialog '${dialogId.name}'")
    show(ctx.getSupportFragmentManager, dialogId.name)
  }
  catch {
    case e: Exception => DebugReporter.debugAndReport(e, s"Error while showing dialog '${dialogId.name}'")
  }

  //TODO  override def onDismiss(dialog: DialogInterface): Unit = {
  //    super.onDismiss(dialog)
  //    DialogWrapper.unregisterWrapper(dialogId)
  //  }

  override def dismiss(): Unit = try {
    super.dismiss()
  }
  catch {
    case e: Exception => DebugReporter.debugAndReport(e, s"Error while hiding dialog '${dialogId.name}'")
  }

  override def onSaveInstanceState(outState: Bundle): Unit = {
    super.onSaveInstanceState(outState)
    outState.putAll(save)
  }

  def getDialogId: Symbol = dialogId
}
