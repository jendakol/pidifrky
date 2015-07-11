package cz.jenda.pidifrky.ui.api

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
sealed trait Orientation

object Orientation {

  import android.view.Surface._

  def apply(o: Int): Orientation = o match {
    case ROTATION_0 | ROTATION_180 => PortraitOrientation
    case ROTATION_90 | ROTATION_270 => LandscapeOrientation
  }
}

case object LandscapeOrientation extends Orientation

case object PortraitOrientation extends Orientation
