package controllers.device

import javax.inject.Inject

import annots.ConfigProperty
import controllers.device.DeviceControllerImplicits._
import cz.jenda.pidifrky.proto.DeviceBackend.DebugReportRequest
import play.api.mvc._
import utils.helpers.{EmailHelper, GZipHelper}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class DeviceReportController @Inject()(emailHelper: EmailHelper, @ConfigProperty("play.mailer.to") emailTo: String, @ConfigProperty("emailSubjects.debugReport") subject: String) extends DeviceController {
  def debug = Action.async { implicit request =>

    val email = deviceRequest(DebugReportRequest.PARSER) flatMap { r =>
        GZipHelper.decompress(r.data.getContent.toByteArray).map(d => (r.data.getContact, new String(d)))
    } flatMap { case (contact, report) =>
      emailHelper.sendMail(subject, emailTo)(None, Some(report))
    }

    email.map(toUnit).toResponse
  }
}
