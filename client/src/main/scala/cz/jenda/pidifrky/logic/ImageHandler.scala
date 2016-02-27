package cz.jenda.pidifrky.logic

import java.io.{File, InputStream}

import android.net.Uri
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.exceptions.ResourceNotFoundException
import cz.jenda.pidifrky.ui.api.BasicActivity

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object ImageHandler {

  import Application.executionContext

  def getKnownImages(implicit ctx: BasicActivity): Future[Seq[Int]] = Transaction.async("get-known-images") {
    StorageHandler.withStorage { st =>
      val big = st.getFiles(PidifrkyConstants.PATH_IMAGES_FULL, ".*\\.jpg").asScala.flatMap { f =>
        f.getName.split("\\.").headOption
      }.map(_.toInt)

      val thumb = st.getFiles(PidifrkyConstants.PATH_IMAGES_THUMBS, ".*\\.jpg").asScala.flatMap { f =>
        f.getName.split("\\.").headOption
      }.map(_.toInt)

      big intersect thumb
    }.andThen {
      case Success(list) => list
      case Failure(e) =>
        DebugReporter.debugAndReport(e, "Error while reading existing images")
        Seq()
    }
  }

  def saveImages(imagesStream: InputStream)(implicit ctx: BasicActivity): Future[Unit] = Transaction.async("save-images") {
    StorageHandler.withStorage { st =>
      TarGzHelper.decompress(imagesStream) { name =>
        if (name.startsWith(PidifrkyConstants.THUMBS_DOWNLOADED_PREFIX)) {
          st.getFile(PidifrkyConstants.PATH_IMAGES_THUMBS + File.separator + name.substring(PidifrkyConstants.THUMBS_DOWNLOADED_PREFIX.length))
        } else {
          st.getFile(PidifrkyConstants.PATH_IMAGES_FULL + File.separator + name)
        }
      }
    }.flatMap(identity)
  }

  def getThumbImageUri(cardId: Int)(implicit ctx: BasicActivity): Future[Uri] = StorageHandler.withStorage { st =>
    val file = st.getFile(PidifrkyConstants.PATH_IMAGES_THUMBS, s"$cardId.jpg")
    if (!file.exists()) throw ResourceNotFoundException(s"thumb for card $cardId")

    Uri.fromFile(file)
  }.recover { case _ => EmptyThumbnailUri }

  def getFullImageUri(cardId: Int)(implicit ctx: BasicActivity): Future[Uri] = StorageHandler.withStorage { st =>
    val file = st.getFile(PidifrkyConstants.PATH_IMAGES_FULL, s"$cardId.jpg")
    if (!file.exists()) throw ResourceNotFoundException(s"image for card $cardId")

    Uri.fromFile(file)
  }.recover { case _ => EmptyImageUri }

  final lazy val EmptyThumbnailUri: Uri = Utils.toUri(R.drawable.empty_thumb)

  final lazy val EmptyImageUri: Uri = Utils.toUri(R.drawable.empty)
}
