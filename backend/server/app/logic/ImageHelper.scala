package logic

import java.nio.file.{Files, Path, Paths}

import annots.{ConfigProperty, StoragePath, BlockingExecutor, CallbackExecutor}
import com.google.inject.Inject
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

  def downloadImagesForCards(cardsNumbers: Seq[Int]): Future[Unit] = Future.sequence(cardsNumbers.map { number =>
    getImage(number) match {
      case Some(path) =>
        Logger.debug(s"No need to download image for card no. $number, it already exists")
        createThumb(path) match {
          case Failure(e) => Logger.warn(s"Cannot create thumbnail image for card no. $number to $dir", e)
          case Success(_) =>
        }
        Future.successful(())
      case None =>
        lock.withLockAsync {
          HttpClient.get(imageUrl.format(number)).andThen {
            case Success(resp) if resp.contentLength.getOrElse(0l) > 50000 =>
              dir.saveNewFile(number + ".jpg", resp.stream) match {
                case Failure(e) => Logger.warn(s"Cannot save image for card no. $number to $dir", e)
                case Success(path) => createThumb(path)
              }

            case Success(resp) =>
              //response of unknown or too small size
              resp.contentLength match {
                case None =>
                  //unknown size, take that risk
                  dir.saveNewFile(number + ".jpg", resp.stream) match {
                    case Failure(e) => Logger.warn(s"Cannot save image for card no. $number to $dir", e)
                    case Success(path) =>
                      if (path.toFile.length() > 50000)
                        createThumb(path)
                      else {
                        Logger.info(s"Some bad image was received for card no. $number")
                        Files.delete(path)
                      }
                  }
                case _ => Logger.info(s"Some bad image was received for card no. $number")
              }

            case Failure(e) => Logger.warn(s"Cannot download image for card no. $number", e)
          }
        }
    }
  }).map(_ => ())

  def getImage(cardNumber: Int): Option[Path] = dir.find(cardNumber + ".jpg")

  def getImages(cardNumbers: Seq[Int]): Seq[Path] = cardNumbers.flatMap { number =>
    dir.find(number + ".jpg")
  }

  def packImages(cardNumbers: Seq[Int]): Map[Int, CardImage] = cardNumbers.flatMap { number =>
    for {
      fullImage <- dir.find(number + ".jpg")
      thumbImage <- dir.find(s"_thumb_$number.jpg")
    } yield {
      (number, CardImage(number, Files.readAllBytes(fullImage), Files.readAllBytes(thumbImage)))
    }
  }.toMap

  def createThumb(fullImage: Path): Try[Path] = Try {
    val thumbPath = fullImage.getParent.toString + "/_thumb_" + fullImage.getFileName

    val path = Paths.get(thumbPath)

    if (Files.exists(path)) {
      Logger.debug(s"No need to create thumbnail image for $fullImage, it already exists")
      return Success(path)
    }

    val opener = new Opener
    val ip = opener.openImage(fullImage.toString).getProcessor

    ip.blurGaussian(0.3)
    ip.setInterpolationMethod(ImageProcessor.NONE)
    val outputProcessor = ip.resize(170, 118)
    IJ.saveAs(new ImagePlus("", outputProcessor), "jpg", thumbPath)

    path
  }
}

case class CardImage(number: Int, bytes: Array[Byte], thumbBytes: Array[Byte])
