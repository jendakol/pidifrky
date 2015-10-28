package cz.jenda.pidifrky.logic

import java.io.{File, InputStream}

import android.net.Uri
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.exceptions.ResourceNotFoundException

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object ImageHandler {

  import Application.executionContext

  def getKnownImages: Future[Seq[Int]] = Transaction.async("get-known-images") {
    Future {
      StorageHandler.withStorage { st =>
        val big = st.getFiles(PidifrkyConstants.PATH_IMAGES_FULL, ".*\\.jpg").asScala.flatMap { f =>
          f.getName.split("\\.").headOption
        }.map(_.toInt)

        val thumb = st.getFiles(PidifrkyConstants.PATH_IMAGES_THUMBS, ".*\\.jpg").asScala.flatMap { f =>
          f.getName.split("\\.").headOption
        }.map(_.toInt)

        big intersect thumb
      } match {
        case Success(list) => list
        case Failure(e) =>
          DebugReporter.debugAndReport(e, "Error while reading existing images")
          Seq()
      }
    }
  }

  def saveImages(imagesStream: InputStream): Future[Unit] = Transaction.async("save-images") {
    Future.fromTry(StorageHandler.withStorage { st =>
      TarGzHelper.decompress(imagesStream) { name =>
        if (name.startsWith(PidifrkyConstants.THUMBS_DOWNLOADED_PREFIX)) {
          st.getFile(PidifrkyConstants.PATH_IMAGES_THUMBS + File.separator + name.substring(PidifrkyConstants.THUMBS_DOWNLOADED_PREFIX.length))
        } else {
          st.getFile(PidifrkyConstants.PATH_IMAGES_FULL + File.separator + name)
        }
      }
    }).flatMap(identity)
  }

  def getThumbImageUri(cardId: Int): Option[Uri] = StorageHandler.withStorage { st =>
    val file = st.getFile(PidifrkyConstants.PATH_IMAGES_THUMBS, s"$cardId.jpg")
    if (!file.exists()) throw ResourceNotFoundException(s"thumb for card $cardId")

    Uri.fromFile(file)
  }.recover { case _ => EmptyThumbnailUri }.toOption

  def getFullImageUri(cardId: Int): Option[Uri] = StorageHandler.withStorage { st =>
    val file = st.getFile(PidifrkyConstants.PATH_IMAGES_FULL, s"$cardId.jpg")
    if (!file.exists()) throw ResourceNotFoundException(s"image for card $cardId")

    Uri.fromFile(file)
  }.recover { case _ => EmptyImageUri }.toOption

  final lazy val EmptyThumbnailUri: Uri = Utils.toUri(R.drawable.empty_thumb)

  final lazy val EmptyImageUri: Uri = Utils.toUri(R.drawable.empty)
}
