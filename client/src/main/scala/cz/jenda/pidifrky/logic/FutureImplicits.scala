package cz.jenda.pidifrky.logic

import android.app.Activity

import scala.concurrent.duration.Duration
import scala.concurrent.{CanAwait, ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object FutureImplicits {

  implicit class PidifrkyFuture[T](future: Future[T]) extends Future[T] {

    def mapFailure(f: PartialFunction[Throwable, Throwable])(implicit executor: ExecutionContext): PidifrkyFuture[T] =
      new PidifrkyFuture[T](future.recoverWith {
        case NonFatal(t) => if (f.isDefinedAt(t)) Future.failed(f.apply(t)) else Future.failed(t)
      })

    def foreachOnUIThread(f: T => Unit)(implicit executor: ExecutionContext, ctx: Activity): Unit =
      future.foreach(r => Utils.runOnUiThread(f(r)))

    override def onComplete[U](f: (Try[T]) => U)(implicit executor: ExecutionContext): Unit = future.onComplete(f)

    override def isCompleted: Boolean = future.isCompleted

    override def value: Option[Try[T]] = future.value

    override def result(atMost: Duration)(implicit permit: CanAwait): T = future.result(atMost)

    override def ready(atMost: Duration)(implicit permit: CanAwait): PidifrkyFuture.this.type = {
      future.ready(atMost)
      this
    }
  }

}
