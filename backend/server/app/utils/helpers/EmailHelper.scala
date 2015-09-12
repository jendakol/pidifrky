package utils.helpers

import javax.inject.Inject

import play.api.libs.mailer._
import play.twirl.api.HtmlFormat
import utils.ConfigProperty

import scala.util.{Failure, Success, Try}

/**
 * @author Jenda Kolena, kolena@avast.com
 */
class EmailHelper @Inject()(mailerClient: MailerClient, @ConfigProperty("play.mailer.from") fromEmail: String) {
  implicit val Logger = play.api.Logger(getClass)

  def sendMail(subject: String, dest: String)(htmlContent: Option[HtmlFormat.Appendable], textContent: Option[String] = None): Try[String] = Try {
    val email = Email(
      subject,
      fromEmail,
      Seq(dest),
      //TODO add attachment
      attachments = Seq(

      ),
      bodyText = textContent,
      bodyHtml = htmlContent.map(_.body)
    )

    mailerClient.send(email)
  }.transform(id => {
    Logger.debug(s"Successfully sent email id $id to $dest")
    Success(id)
  }, t => {
    Logger.debug(s"Email could not be sent", t)
    Failure(t)
  })
}
