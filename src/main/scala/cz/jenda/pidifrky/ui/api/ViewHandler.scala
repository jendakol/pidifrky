package cz.jenda.pidifrky.ui.api

import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import cz.jenda.pidifrky.logic.DebugReporter
import cz.jenda.pidifrky.logic.exceptions.AnotherTypeOfViewException

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait ViewHandler extends AppCompatActivity {
  def findView(id: Int): Option[View] = Option(findViewById(id))

  def findTextView(id: Int): Option[TextView] = findView(id) flatMap {
    case v: TextView => Some(v)
    case _ =>
      DebugReporter.debug(AnotherTypeOfViewException)
      None
  }

}
