package cz.jenda.pidifrky.logic

import java.util.concurrent.Executors

import cz.jenda.pidifrky.ui.api.{BasicActivity, Orientation, PortraitOrientation}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object Application {
  var currentActivity: Option[BasicActivity] = None
  var currentOrientation: Orientation = PortraitOrientation

  implicit val executionContext: ExecutionContextExecutorService = ExecutionContext.fromExecutorService(Executors.newScheduledThreadPool(Runtime.getRuntime.availableProcessors()))
}
