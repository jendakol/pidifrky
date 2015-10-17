package utils

import java.util.concurrent.{Executor, TimeUnit}

import com.google.common.util.concurrent.{FutureCallback, Futures}
import com.google.common.util.{concurrent => google}
import com.ning.http.client.{ListenableFuture, Response}
import logic.HttpResponse

import scala.concurrent.{Future, Promise}
import scala.language.implicitConversions

/**
  * @author Jenda Kolena, kolena@avast.com
  */
object Implicits {
  implicit def ningListenableToScalaFuture(l: ListenableFuture[Response]): Future[HttpResponse] = {
    val p = Promise[HttpResponse]()

    Futures.addCallback(l, new FutureCallback[Response] {
      override def onFailure(t: Throwable) = p.failure(t)

      override def onSuccess(result: Response) = {
        val response = HttpResponse(result.getResponseBodyAsStream, Option(result.getHeader("Content-Length")).map(_.toLong))

        p.success(response)
      }
    })

    p.future
  }

  implicit def ningListenableToGuavaListenable[A](l: ListenableFuture[A]): google.ListenableFuture[A] =
    new google.ListenableFuture[A] {
      override def addListener(listener: Runnable, executor: Executor): Unit = l.addListener(listener, executor)

      override def isCancelled: Boolean = l.isCancelled

      override def get(): A = l.get()

      override def get(timeout: Long, unit: TimeUnit): A = l.get(timeout, unit)

      override def cancel(mayInterruptIfRunning: Boolean): Boolean = l.cancel(mayInterruptIfRunning)

      override def isDone: Boolean = l.isDone
    }
}
