package cz.jenda.pidifrky.logic.dbimport

import java.io.FileNotFoundException

import android.app.DownloadManager
import android.content.{BroadcastReceiver, Context, Intent}
import android.net.Uri
import android.os.ParcelFileDescriptor
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic._
import cz.jenda.pidifrky.logic.http.DeviceEnvelopeConverter
import cz.jenda.pidifrky.proto.DeviceBackend.ImageDownloadRequest
import cz.jenda.pidifrky.ui.api.{BasicActivity, LoadToast, PidifrkyPermissions, Toast}

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object DownloadHandler extends BroadcastReceiver {

  import Application._

  private lazy val DownloadImagesUri = Uri.parse(PidifrkyConstants.URL_DOWNLOAD_IMAGES)

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

  def downloadImages(payload: ImageDownloadRequest)(implicit ctx: BasicActivity): Future[Unit] = {
    ctx.requestPermission(PidifrkyPermissions.ExternalStorage).flatMap { _ =>
      Future.fromTry {
        Try {
          val env = DeviceEnvelopeConverter.wrapByEnvelope(payload)

          val req = new DownloadManager.Request(DownloadImagesUri)
            .setVisibleInDownloadsUi(false)
            .setTitle(ctx.getString(R.string.downloading_images))
            .setDestinationInExternalFilesDir(ctx, "", DownloadHandler.ImagesFileName)
            .addRequestHeader(PidifrkyConstants.HEADER_PAYLOAD, Utils.bytes2hex(env))

          Ids.downloadImages = downloadManager.enqueue(req)
        }
      }
    }.andThen { case Success(_) =>
      DebugReporter.debug("Download images request has been sent")
      Toast(R.string.downloading_images, Toast.Short)
    }
  }

  override def onReceive(context: Context, intent: Intent): Unit = withCurrentContext { implicit ctx =>
    val ref = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

    if (ref == Ids.downloadImages) {
      LoadToast.withLoadToast(R.string.downloading_images_saving) {
        DebugReporter.debug("Received update of card images")
        Future {
          val f = downloadManager.openDownloadedFile(ref)

          new ParcelFileDescriptor.AutoCloseInputStream(f)
        }.flatMap(ImageHandler.saveImages).andThen {
          case Success(_) =>
            //TODO better message then toast?
            Application.withCurrentContext(Toast(R.string.downloading_images_done, Toast.Long)(_))
          case Failure(e: FileNotFoundException) =>
            Application.withCurrentContext(Toast(R.string.downloading_images_fail, Toast.Long)(_))
          case Failure(NonFatal(e)) =>
            DebugReporter.debugAndReport(e, "Error while downloading images")
            //TODO better message then toast?
            Application.withCurrentContext(Toast(Format(e), Toast.Long)(_))
        }
      }
    } else {
      DebugReporter.debug("Unknown download ref ID")
    }
  }
}
