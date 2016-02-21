package cz.jenda.pidifrky.logic

import android.app.Activity

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object FutureImplicits {

  implicit class PidifrkyFuture[T](val future: Future[T]) extends AnyVal {

    def mapFailure(f: PartialFunction[Throwable, Throwable])(implicit executor: ExecutionContext): PidifrkyFuture[T] =
      new PidifrkyFuture[T](future.recoverWith {
        case NonFatal(t) => if (f.isDefinedAt(t)) Future.failed(f.apply(t)) else Future.failed(t)
      })

    def foreachOnUIThread(f: T => Unit)(implicit executor: ExecutionContext, ctx: Activity): Unit = if (ctx != null) {
      future.foreach(r => Utils.runOnUiThread(f(r)))
    } else {
      DebugReporter.debug("Cannot invoke callback on UI thread, null context passed")
    }
  }

}
