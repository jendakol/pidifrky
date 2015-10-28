package controllers

import javax.inject.Inject

import annots.CallbackExecutor
import data.Dao
import logic.Updater
import play.api.mvc.{Action, Controller}
import utils.Format

import scala.concurrent.ExecutionContext

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class TestController @Inject()(cardsDao: Dao, updater: Updater, @CallbackExecutor implicit val ec: ExecutionContext) extends Controller {
  def test = Action.async { request =>
    updater.update().map(_ => Ok("ok")).recover {
      case t: Exception => InternalServerError(Format(t) + "\nat " + t.getStackTrace.mkString("\nat "))
    }
//
//    Future.successful(Ok("ok"))
  }
}
