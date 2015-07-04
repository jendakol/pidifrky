package cz.jenda.pidifrky.ui.api

import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import cz.jenda.pidifrky.logic.DebugReporter
import cz.jenda.pidifrky.logic.exceptions.{AnotherTypeOfViewException, CannotFindViewException}

import scala.reflect.ClassTag

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait ViewHandler extends AppCompatActivity {

  def findView(id: Int): Option[View] = ViewHandler.findView(this, id)

  def findView[T <: View](id: Int, cl: Class[T])(implicit ct: ClassTag[T]): Option[T] = ViewHandler.findView(this, id, cl)

  def findTextView(id: Int): Option[TextView] = ViewHandler.findView(this, id, classOf[TextView])

}

object ViewHandler {
  def findView(activity: AppCompatActivity, id: Int): Option[View] = Option(activity.findViewById(id))

  def findView[T <: View](activity: AppCompatActivity, id: Int, cl: Class[T])(implicit ct: ClassTag[T]): Option[T] = findView(activity, id) flatMap {
    case v: View =>
      if (ct.runtimeClass.isInstance(v)) {
        Some(v.asInstanceOf[T])
      }
      else {
        DebugReporter.debug(AnotherTypeOfViewException)
        None
      }
    case _ =>
      DebugReporter.debug(CannotFindViewException)
      None
  }

  def findTextView(activity: AppCompatActivity, id: Int): Option[TextView] = findView(activity, id, classOf[TextView])

}
