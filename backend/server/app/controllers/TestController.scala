package controllers

import javax.inject.Inject

import data.dao.CardsDao
import play.api.mvc.{Action, Controller}

/**
 * @author Jenda Kolena, kolena@avast.com
 */
class TestController @Inject()(cardsDao: CardsDao) extends Controller {
  def test = Action {
    Ok("ok")
  }
}
