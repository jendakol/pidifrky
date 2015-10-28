package cz.jenda.pidifrky.logic.dbimport

import java.io.FileNotFoundException

import android.app.DownloadManager
import android.content.{BroadcastReceiver, Context, Intent}
import android.net.Uri
import android.os.ParcelFileDescriptor
import cz.jenda.pidifrky.logic._
import cz.jenda.pidifrky.logic.http.DeviceEnvelopeConverter
import cz.jenda.pidifrky.proto.DeviceBackend.ImageDownloadRequest
import cz.jenda.pidifrky.ui.api.BasicActivity

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object DownloadHandler extends BroadcastReceiver {

  import Application.executionContext

  private object Ids {
    var downloadImages: Long = _
  }

  private final val ImagesFileName = "images.tar.gz"

  protected val downloadManager: DownloadManager = Application.appContext.orElse(Application.currentActivity).map { ctx =>
    ctx.getSystemService(Context.DOWNLOAD_SERVICE).asInstanceOf[DownloadManager]
  }.getOrElse {
    DebugReporter.debugAndReport(new Exception("Missing context for download manager"))
    null
  }

  def downloadImages(payload: ImageDownloadRequest)(implicit ctx: BasicActivity): Try[Unit] = Try {
    val env = DeviceEnvelopeConverter.wrapByEnvelope(payload)

    val req = new DownloadManager.Request(Uri.parse(PidifrkyConstants.URL_DOWNLOAD_IMAGES))
      .setVisibleInDownloadsUi(false)
      .setDestinationInExternalFilesDir(ctx, "", DownloadHandler.ImagesFileName)
      .addRequestHeader(PidifrkyConstants.HEADER_PAYLOAD, Utils.bytes2hex(env))

    Ids.downloadImages = downloadManager.enqueue(req)
  }

  override def onReceive(context: Context, intent: Intent): Unit = {
    val ref = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

    if (ref == Ids.downloadImages) {
      DebugReporter.debug("Received update of card images")
      Future {
        val f = downloadManager.openDownloadedFile(ref)

        new ParcelFileDescriptor.AutoCloseInputStream(f)
      }.flatMap(ImageHandler.saveImages).andThen {
        case Success(_) =>
          //TODO better message then toast?
          Application.currentActivity.foreach(Toast("Images downloaded", Toast.Medium)(_))
        case Failure(e: FileNotFoundException) =>
          Application.currentActivity.foreach(Toast("Could not download the file", Toast.Medium)(_))
        case Failure(NonFatal(e)) =>
          DebugReporter.debugAndReport(e, "Error while downloading images")
          //TODO better message then toast?
          Application.currentActivity.foreach(Toast(Format(e), Toast.Medium)(_))
      }
    } else {
      DebugReporter.debug("Unknown download ref ID")
    }
  }
}
