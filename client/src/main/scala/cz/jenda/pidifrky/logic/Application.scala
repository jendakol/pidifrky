package cz.jenda.pidifrky.logic

import java.util.concurrent.atomic.AtomicBoolean

import android.content.Context
import cz.jenda.pidifrky.data.Database
import cz.jenda.pidifrky.logic.exceptions.OfflineException
import cz.jenda.pidifrky.ui.api.{BasicActivity, Orientation, PortraitOrientation}

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object Application {
  var appContext: Option[Context] = None
  var currentActivity: Option[BasicActivity] = None
  var currentOrientation: Orientation = PortraitOrientation

  private val initialized = new AtomicBoolean(false)

  implicit val executionContext: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(
    new ForkJoinPool(math.max(8, math.min(3, Runtime.getRuntime.availableProcessors())))
  )

  def withOnlineStatus[T](block: => Future[T]): Future[T] =
    (if (Utils.isOnline) Future.successful(true) else Utils.updateOnlineStatus()).flatMap { online =>
      if (!online) Future.failed(OfflineException)
      else
        block
    }

  def withCurrentContext[A](action: BasicActivity => A): Option[A] = {
    currentActivity.map(action)
  }

  //noinspection ScalaUselessExpression
  def init(implicit act: BasicActivity): Future[Boolean] = initialized.synchronized {
    if (!initialized.get()) appContext.map { implicit ctx =>
      DebugReporter.debug("Running Application.init")
      initialized.set(true)

      for {
        _ <- Future {
          Database
          PidifrkySettings.markAndGetStarts
        }
        _ <- StorageHandler.init
      } yield {
        DebugReporter.debug("Application has been initialized")
        true
      }
    }.getOrElse(Future.failed(new IllegalStateException("AppContext is not set yet")))
    else {
      DebugReporter.debug("Application is already initialized")
      Future.successful(false)
    }
  }
}
