package cz.jenda.pidifrky.logic

import android.app.Activity
import cz.jenda.pidifrky.logic.exceptions.TimeoutException

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

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

    def flatMapOnUIThread[Result](f: T => Future[Result])(implicit executor: ExecutionContext, ctx: Activity): Future[Result] = if (ctx != null) {
      future.flatMap(r => Utils.runOnUiThread(f(r))).flatMap(identity)
    } else {
      DebugReporter.debug("Cannot invoke callback on UI thread, null context passed")
      Future.failed(new IllegalArgumentException("Cannot invoke action on UI thread, null context passed"))
    }

    def mapOnUIThread[Result](f: T => Result)(implicit executor: ExecutionContext, ctx: Activity): Future[Result] = if (ctx != null) {
      future.map(r => Utils.runOnUiThread(f(r))).flatMap(identity)
    } else {
      DebugReporter.debug("Cannot invoke callback on UI thread, null context passed")
      Future.failed(new IllegalArgumentException("Cannot invoke action on UI thread, null context passed"))
    }

    def andThenOnUIThread(pf: PartialFunction[Try[T], Unit])(implicit executor: ExecutionContext, ctx: Activity): Future[T] = {
      if (ctx != null) {
        future.andThen {
          case Success(r) => Utils.runOnUiThread(pf(Success(r)))
          case Failure(NonFatal(e)) => Utils.runOnUiThread(pf(Failure(e)))
        }
      } else {
        DebugReporter.debug("Cannot invoke callback on UI thread, null context passed")
      }

      future
    }

    def block: T = {
      try {
        Await.result(future, 2.seconds)
      }
      catch {
        case e: TimeoutException =>
          DebugReporter.debug(e, "Task wasn't completed in 2 seconds")
          throw e
      }
    }
  }

}
