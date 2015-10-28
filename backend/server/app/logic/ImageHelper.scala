package logic

import java.nio.file.{Files, Path, Paths}

import annots.{BlockingExecutor, CallbackExecutor, ConfigProperty, StoragePath}
import com.google.inject.Inject
import data.CardPojo
import ij.io.{FileSaver, Opener}
import ij.process.ImageProcessor
import ij.{IJ, ImagePlus}
import utils.{Logging, StorageDir}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}


/**
  * @author Jenda Kolena, jendakolena@gmail.com
  */
class ImageHelper @Inject()(@ConfigProperty("url.pidifrk.image") imageUrl: String, @StoragePath("pidifrkImages") dir: StorageDir, @BlockingExecutor blocking: ExecutionContext, @CallbackExecutor implicit val ec: ExecutionContext) extends Logging {
  require(dir.isWriteable)

  FileSaver.setJpegQuality(75)

  final val ThumbSize = 60
  final val NormalSizeWidth = 720
  final val NormalSizeHeight = 720

  protected val lock = new Semaphore(20)

  def downloadImagesForCards(cardsIds: Seq[CardPojo]): Future[Unit] = Future.sequence(cardsIds.map { card =>
    val id = card.id
    val number = card.number

    getBigImage(id) match {
      case Some(path) =>
        Logger.debug(s"No need to download image for card ID $id, it already exists")
        processImage(id, path)
        Future.successful(())
      case None =>
        lock.withLockAsync {
          HttpClient.get(imageUrl.format(number)).andThen {
            case Success(resp) if resp.contentLength.getOrElse(0l) > 50000 =>
              dir.saveNewFile(s"_big_$id.jpg", resp.stream) match {
                case Failure(e) => Logger.warn(s"Cannot save image for card ID $id to $dir", e)
                case Success(path) =>
                  Logger.debug(s"Image for card ID $id downloaded")
                  processImage(id, path)
              }

            case Success(resp) =>
              //response of unknown or too small size
              resp.contentLength match {
                case None =>
                  //unknown size, take that risk
                  dir.saveNewFile(s"_big_$id.jpg", resp.stream) match {
                    case Failure(e) => Logger.warn(s"Cannot save image for card ID $id to $dir", e)
                    case Success(path) =>
                      if (path.toFile.length() > 50000)
                        processImage(id, path)
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

  private def processImage(id: Int, path: Path): Unit = (for {
    _ <- createNormal(path)
    _ <- createThumb(path)
  } yield ()) match {
    case Failure(e) => Logger.warn(s"Error while processing image for card ID $id", e)
    case Success(_) => Logger.debug(s"Image for card ID $id processed")
  }

  def getBigImage(cardNumber: Int): Option[Path] = dir.find(s"_big_$cardNumber.jpg")

  def loadImages(cardNumbers: Seq[Int], includeFull: Boolean): Future[Seq[Path]] = Future {
    cardNumbers.flatMap { id =>
      if (includeFull) {
        Seq(dir.find(id + ".jpg"), dir.find(s"_thumb_$id.jpg"))
      } else {
        Seq(dir.find(s"_thumb_$id.jpg"))
      }
    }.flatten
  }(blocking)

  def createThumb(fullImage: Path): Try[Path] = Try {
    val thumbPath = fullImage.getParent.toString + "/_thumb_" + fullImage.getFileName.toString.substring(BigPrefixLength)

    Logger.debug(s"Creating thumbnail image from $fullImage")

    val path = Paths.get(thumbPath)

    if (Files.exists(path)) {
      Logger.debug(s"No need to create thumbnail image for $fullImage, it already exists")
      return Success(path)
    }

    val opener = new Opener
    val ip = opener.openImage(fullImage.toString).getProcessor
    ip.setInterpolationMethod(ImageProcessor.BILINEAR)

    val normalized = if (ip.getWidth > NormalSizeWidth) {
      ip.blurGaussian(0.15)
      ip.resize(NormalSizeWidth)
    } else ip

    normalized.blurGaussian(1.1)
    val resized = normalized.resize((ThumbSize * (ip.getWidth / ip.getHeight.toDouble)).toInt, ThumbSize)

    resized.setRoi((resized.getWidth - ThumbSize) / 2, (resized.getHeight - ThumbSize) / 2, ThumbSize, ThumbSize)

    val cropped = resized.crop()

    IJ.saveAs(new ImagePlus("", cropped), "jpg", thumbPath)

    path
  }

  private val BigPrefixLength = "_big_".length


  def createNormal(fullImage: Path): Try[Path] = Try {
    val thumbPath = fullImage.getParent.toString + "/" + fullImage.getFileName.toString.substring(BigPrefixLength)

    Logger.debug(s"Creating normal image from $fullImage")

    val path = Paths.get(thumbPath)

    if (Files.exists(path)) {
      Logger.debug(s"No need to create normal image for $fullImage, it already exists")
      return Success(path)
    }

    val opener = new Opener
    val ip = opener.openImage(fullImage.toString).getProcessor
    ip.setInterpolationMethod(ImageProcessor.BILINEAR)

    val normalized = if (ip.getWidth != NormalSizeWidth) {
      ip.blurGaussian(0.15)
      ip.resize(NormalSizeWidth)
    } else ip

    normalized.setRoi((normalized.getWidth - NormalSizeWidth) / 2, (normalized.getHeight - NormalSizeHeight) / 2, NormalSizeWidth, NormalSizeHeight)

    val cropped = normalized.crop()

    IJ.saveAs(new ImagePlus("", cropped), "jpg", thumbPath)

    path
  }
}

case class CardImage(id: Int, bytes: Array[Byte], thumbBytes: Array[Byte])
