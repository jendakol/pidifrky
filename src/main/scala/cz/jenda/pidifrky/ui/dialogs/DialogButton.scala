package cz.jenda.pidifrky.ui.dialogs

import android.os.{Parcel, Parcelable}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
sealed trait DialogButton extends Parcelable {
  protected val t: Int
  val id: Int
  val stringId: Int

  override def describeContents(): Int = id * 16 + stringId * 32

  override def writeToParcel(dest: Parcel, flags: Int): Unit = {
    dest.writeInt(t)
    dest.writeInt(id)
    dest.writeInt(stringId)
  }
}

object DialogButton {
  val CREATOR: Parcelable.Creator[DialogButton] = new Parcelable.Creator[DialogButton] {
    override def createFromParcel(parcel: Parcel): DialogButton = parcel.readInt() match {
      case 0 => PositiveDialogButton(parcel.readInt(), parcel.readInt())
      case 1 => NeutralDialogButton(parcel.readInt(), parcel.readInt())
      case 2 => NegativeDialogButton(parcel.readInt(), parcel.readInt())
    }

    override def newArray(size: Int): Array[DialogButton] = new Array[DialogButton](size)
  }
}

case class PositiveDialogButton(id: Int, stringId: Int) extends DialogButton {
  override protected val t: Int = 0
}

case class NeutralDialogButton(id: Int, stringId: Int) extends DialogButton {
  override protected val t: Int = 1
}

case class NegativeDialogButton(id: Int, stringId: Int) extends DialogButton {
  override protected val t: Int = 2
}