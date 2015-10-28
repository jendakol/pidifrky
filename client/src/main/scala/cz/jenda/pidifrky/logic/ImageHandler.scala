package cz.jenda.pidifrky.logic

import java.io.{File, InputStream}

import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * @author Jenda Kolena, kolena@avast.com
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




    //    Future {
    //      Future.fromTry(StorageHandler.withStorage { st =>
    //        Future.sequence(images.map { im =>
    //          for {
    //            _ <- if (im.hasFullImageBytes) Try(st.createFile(PidifrkyConstants.PATH_IMAGES_FULL, im.getCardId + ".jpg", im.getFullImageBytes.toByteArray)) else Success(())
    //            _ <- Try(st.createFile(PidifrkyConstants.PATH_IMAGES_THUMBS, im.getCardId + ".jpg", im.getThumbnailBytes.toByteArray))
    //          } yield ()
    //        }.map(Future.fromTry)).map(_ => ())
    //      }).flatMap(identity)
    //    }.flatMap(identity)
  }
}
