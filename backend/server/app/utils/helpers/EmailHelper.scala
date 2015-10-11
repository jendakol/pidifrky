package utils.helpers

import javax.inject.Inject

import play.api.libs.mailer._
import play.twirl.api.HtmlFormat
import utils.{ConfigProperty, Logging}

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * @author Jenda Kolena, kolena@avast.com
 */
class EmailHelper @Inject()(mailerClient: MailerClient, @ConfigProperty("play.mailer.from") fromEmail: String) extends Logging {

  import logic.AppModule._

  def sendMail(subject: String, dest: String)(htmlContent: Option[HtmlFormat.Appendable], textContent: Option[String] = None): Future[String] = Future {
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
  }(blockingExecutor).andThen {
    case Success(id) => Logger.debug(s"Successfully sent email id $id to $dest")
    case Failure(t) => Logger.debug(s"Email could not be sent", t)
  }
}
