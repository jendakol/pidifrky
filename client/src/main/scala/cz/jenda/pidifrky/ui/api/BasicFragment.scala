package cz.jenda.pidifrky.ui.api

import android.os.Bundle
import android.support.v4.app.Fragment
import cz.jenda.pidifrky.logic.DebugReporter

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicFragment extends Fragment with FragmentViewHandler {
  protected final implicit val ElemId: ElementId = ElementId()

  private var ctx: Option[BasicActivity] = None

  override def onActivityCreated(savedInstanceState: Bundle): Unit = {
    super.onActivityCreated(savedInstanceState)
    DebugReporter.debug("Parent activity is created")

    getActivity match {
      case a: BasicActivity =>
        ctx = Option(a)
        DebugReporter.debug(s"${getClass.getSimpleName} attached to ${a.getClass.getSimpleName}")
        super.onResume()
      case _ => throw new IllegalArgumentException(s"Unsupported attachment of fragment to ${getActivity.getClass.getName}")
    }
  }

  protected def withCurrentActivity[A](a: BasicActivity => A): A = {
    ctx.map(a)
      .getOrElse {
        throw new IllegalStateException("Action could not be performed, because the context is missing")
      }
  }

  protected def withCurrentActivityIfPossible[A](a: BasicActivity => A): Option[A] = {
    ctx.map(a)
  }

  protected def invalidateOptionsMenu(): Unit = {
    ctx.foreach(_.invalidateOptionsMenu()) //don't throw exception!
  }
}
