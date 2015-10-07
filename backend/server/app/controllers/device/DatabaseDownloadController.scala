package controllers.device

import javax.inject.Inject

import controllers.device.DeviceControllerImplicits._
import cz.jenda.pidifrky.proto.DeviceBackend.DatabaseUpdateRequest
import play.api.mvc.Action

/**
 * @author Jenda Kolena, kolena@avast.com
 */
class DatabaseDownloadController @Inject()() extends DeviceController {

  def download = Action { implicit request =>
    //TODO
    deviceRequest(DatabaseUpdateRequest.PARSER).map(toUnit).toResponse
  }
}
