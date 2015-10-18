package logic

import java.nio.file.{Files, Path, Paths}

import annots.{BlockingExecutor, CallbackExecutor, ConfigProperty, StoragePath}
import com.google.inject.Inject
import data.CardPojo
import ij.io.Opener
import ij.process.ImageProcessor
import ij.{IJ, ImagePlus}
import utils.{Logging, StorageDir}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


/**
  * @author Jenda Kolena, kolena@avast.com
  */
class ImageHelper @Inject()(@ConfigProperty("url.pidifrk.image") imageUrl: String, @StoragePath("pidifrkImages") dir: StorageDir, @BlockingExecutor blocking: ExecutionContext, @CallbackExecutor implicit val ec: ExecutionContext) extends Logging {
  require(dir.isWriteable)

  protected val lock = new Semaphore(20)

  def downloadImagesForCards(cardsIds: Seq[CardPojo]): Future[Unit] = Future.sequence(cardsIds.map { card =>
    val id = card.id
    val number = card.number

    getImage(id) match {
      case Some(path) =>
        Logger.debug(s"No need to download image for card ID $id, it already exists")
        createThumb(path) match {
          case Failure(e) => Logger.warn(s"Cannot create thumbnail image for card ID $id to $dir", e)
          case Success(p) => Logger.debug(s"Thumbnail for card ID $id ($p) created")
        }
        Future.successful(())
      case None =>
        lock.withLockAsync {
          HttpClient.get(imageUrl.format(number)).andThen {
            case Success(resp) if resp.contentLength.getOrElse(0l) > 50000 =>
              dir.saveNewFile(id + ".jpg", resp.stream) match {
                case Failure(e) => Logger.warn(s"Cannot save image for card ID $id to $dir", e)
                case Success(path) =>
                  Logger.debug(s"Image for card ID $id downloaded")
                  createThumb(path)
              }

            case Success(resp) =>
              //response of unknown or too small size
              resp.contentLength match {
                case None =>
                  //unknown size, take that risk
                  dir.saveNewFile(id + ".jpg", resp.stream) match {
                    case Failure(e) => Logger.warn(s"Cannot save image for card ID $id to $dir", e)
                    case Success(path) =>
                      if (path.toFile.length() > 50000)
                        createThumb(path) match {
                          case Failure(e) => Logger.warn(s"Cannot create thumbnail image for card ID $id to $dir", e)
                          case Success(p) => Logger.debug(s"Thumbnail for card ID $id ($p) created")
                        }
                      else {
                        Logger.info(s"Some bad image was received for card ID $id")
                        Files.delete(path)
                      }
                  }
                case _ => Logger.info(s"Some bad image was received for card ID $id")
              }

            case Failure(e) => Logger.warn(s"Cannot download image for card ID $id", e)
          }
        }
    }
  }).map(_ => ())

  def getImage(cardNumber: Int): Option[Path] = dir.find(cardNumber + ".jpg")

  def loadImages(cardNumbers: Seq[Int]): Future[Seq[CardImage]] = Future {
    cardNumbers.flatMap { id =>
      for {
        fullImage <- dir.find(id + ".jpg")
        thumbImage <- dir.find(s"_thumb_$id.jpg")
      } yield {
        CardImage(id, Files.readAllBytes(fullImage), Files.readAllBytes(thumbImage))
      }
    }
  }(blocking)

  def createThumb(fullImage: Path): Try[Path] = Try {
    val thumbPath = fullImage.getParent.toString + "/_thumb_" + fullImage.getFileName

    Logger.debug(s"Creating thumbnail from $fullImage")

    val path = Paths.get(thumbPath)

    if (Files.exists(path)) {
      Logger.debug(s"No need to create thumbnail image for $fullImage, it already exists")
      return Success(path)
    }

    val opener = new Opener
    val ip = opener.openImage(fullImage.toString).getProcessor

    ip.blurGaussian(1.2)
    ip.setInterpolationMethod(ImageProcessor.NONE)
    val outputProcessor = ip.resize(170, 118)
    IJ.saveAs(new ImagePlus("", outputProcessor), "jpg", thumbPath)

    path
  }
}

case class CardImage(id: Int, bytes: Array[Byte], thumbBytes: Array[Byte])
