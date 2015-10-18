package cz.jenda.pidifrky.logic

import android.content.Context
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

  implicit val executionContext: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(
    new ForkJoinPool(math.max(2, Runtime.getRuntime.availableProcessors()))
  )

  def withOnlineStatus[T](block: => Future[T]): Future[T] =
    (if (Utils.isOnline) Future.successful(true) else Utils.updateOnlineStatus()).flatMap { online =>
      if (!online) Future.failed(OfflineException)
      else
        block
    }
}
