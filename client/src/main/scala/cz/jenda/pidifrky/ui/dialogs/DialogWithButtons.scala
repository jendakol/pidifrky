package cz.jenda.pidifrky.ui.dialogs

import android.os.{Bundle, Parcel, Parcelable}
import android.support.v4.app.FragmentActivity
import com.afollestad.materialdialogs.MaterialDialog.{Builder, SingleButtonCallback}
import com.afollestad.materialdialogs.{DialogAction, MaterialDialog}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.DebugReporter

import scala.concurrent.Promise
import scala.util.Try

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class DialogWithButtons extends DialogWithMessage {

  final protected var dialogType: DialogType = InfoDialogType

  final protected var positiveButton: Option[PositiveDialogButton] = None

  final protected var neutralButton: Option[NeutralDialogButton] = None

  final protected var negativeButton: Option[NegativeDialogButton] = None

  final protected var dialogConfirmedCallback: Option[DialogConfirmedCallback] = None

  final private[dialogs] val dialogConfirmedPromise: Promise[DialogButton] = Promise()


  override def withActivity(ctx: FragmentActivity): this.type = {
    super.withActivity(ctx)

    ctx match {
      case x: DialogConfirmedCallback => dialogConfirmedCallback = Some(x)
      case _ =>
    }

    this
  }

  def withDialogType(dialogType: DialogType): this.type = {
    this.dialogType = dialogType
    this
  }

  def withPositiveButton(button: PositiveDialogButton): this.type = {
    this.positiveButton = Some(button)
    this
  }

  def withNeutralButton(button: NeutralDialogButton): this.type = {
    this.neutralButton = Some(button)
    this
  }

  def withNegativeButton(button: NegativeDialogButton): this.type = {
    this.negativeButton = Some(button)
    this
  }

  override protected def save: Bundle = {
    val bundle = super.save
    bundle.putParcelable("dialogType", dialogType)

    positiveButton.foreach(b => bundle.putParcelable("positiveButton", b))
    neutralButton.foreach(b => bundle.putParcelable("neutralButton", b))
    negativeButton.foreach(b => bundle.putParcelable("negativeButton", b))


    bundle
  }

  override protected def restore(savedState: Option[Bundle]): Unit = {
    super.restore(savedState)

    savedState.foreach { bundle =>
      positiveButton = Option(bundle.getParcelable("positiveButton"))
      neutralButton = Option(bundle.getParcelable("neutralButton"))
      negativeButton = Option(bundle.getParcelable("negativeButton"))
    }
  }

  override protected def createDialogBuilder: Builder = {
    val builder = super.createDialogBuilder
      .iconRes(dialogType.iconId)

    positiveButton.foreach(b => builder.positiveText(b.stringId))
    neutralButton.foreach(b => builder.neutralText(b.stringId))
    negativeButton.foreach(b => builder.negativeText(b.stringId))

    builder.onNegative(new SingleButtonCallback {
      override def onClick(dialog: MaterialDialog, which: DialogAction): Unit = try {
        negativeButton.foreach { button =>
          dialogConfirmedCallback.foreach(_.onDialogConfirmed(dialogId, dialog, button))
          dialogConfirmedPromise.complete(Try(button))
        }
      }
      catch {
        case e: Exception => DebugReporter.debugAndReport(e, "Error while executing button callback")
      }
    }).onNeutral(new SingleButtonCallback {
      override def onClick(dialog: MaterialDialog, which: DialogAction): Unit = try {
        neutralButton.foreach { button =>
          dialogConfirmedCallback.foreach(_.onDialogConfirmed(dialogId, dialog, button))
          dialogConfirmedPromise.complete(Try(button))
        }
      }
      catch {
        case e: Exception => DebugReporter.debugAndReport(e, "Error while executing button callback")
      }
    }).onPositive(new SingleButtonCallback {
      override def onClick(dialog: MaterialDialog, which: DialogAction): Unit = try {
        positiveButton.foreach { button =>
          dialogConfirmedCallback.foreach(_.onDialogConfirmed(dialogId, dialog, button))
          dialogConfirmedPromise.complete(Try(button))
        }
      }
      catch {
        case e: Exception => DebugReporter.debugAndReport(e, "Error while executing button callback")
      }
    })
  }
}

sealed trait DialogType extends Parcelable {
  protected val t: Int
  val iconId: Int

  override def describeContents(): Int = 36 * t

  override def writeToParcel(dest: Parcel, flags: Int): Unit = dest.writeInt(t)
}

object DialogType {
  val CREATOR: Parcelable.Creator[DialogType] = new Parcelable.Creator[DialogType] {
    override def createFromParcel(parcel: Parcel): DialogType = parcel.readInt() match {
      case 0 => InfoDialogType
      case 1 => WarningDialogType
      case 2 => ErrorDialogType
    }

    override def newArray(size: Int): Array[DialogType] = new Array[DialogType](size)
  }
}

case object InfoDialogType extends DialogType {
  override protected val t: Int = 0
  override val iconId: Int = R.drawable.ic_info_outline_white_36dp
}

case object WarningDialogType extends DialogType {
  override protected val t: Int = 1
  override val iconId: Int = R.drawable.ic_warning_white_36dp
}

case object ErrorDialogType extends DialogType {
  override protected val t: Int = 2
  override val iconId: Int = R.drawable.ic_error_outline_white_36dp
}

