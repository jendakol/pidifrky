package cz.jenda.pidifrky.logic

import android.content.Context
import cz.jenda.pidifrky.ui.api.{BasicActivity, Orientation, PortraitOrientation}

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

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
}
