package cz.jenda.pidifrky.ui.api

import android.os.Bundle
import android.support.v4.app.Fragment
import cz.jenda.pidifrky.logic.DebugReporter

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicFragment extends Fragment {
  protected final implicit val ElemId: ElementId = ElementId()

  protected implicit var ctx: BasicActivity = _


  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    getActivity match {
      case a: BasicActivity =>
        ctx = a
        DebugReporter.debug(s"${getClass.getSimpleName} attached to ${a.getClass.getSimpleName}")
        super.onResume()
      case _ => throw new IllegalArgumentException(s"Unsupported attachment of fragment to ${getActivity.getClass.getName}")
    }
  }

  //
  //  override def onResume(): Unit = getActivity match {
  //    case a: BasicActivity =>
  //      ctx = a
  //      DebugReporter.debug(s"${getClass.getSimpleName} attached to ${a.getClass.getSimpleName}")
  //      super.onResume()
  //    case _ => throw new IllegalArgumentException(s"Unsupported attachment of fragment to ${getActivity.getClass.getName}")
  //  }
}
