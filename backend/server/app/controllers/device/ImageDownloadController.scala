package controllers.device

import javax.inject.Inject

import controllers.device.DeviceControllerImplicits._
import cz.jenda.pidifrky.proto.DeviceBackend.ImageDownloadResponse.CardImage
import cz.jenda.pidifrky.proto.DeviceBackend.{ImageDownloadRequest, ImageDownloadResponse}
import logic.ImageHelper
import play.api.mvc.Action
import utils.helpers.{GZipHelper, TarGzHelper}

import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
  * @author Jenda Kolena, kolena@avast.com
  */
class ImageDownloadController @Inject()(imageHelper: ImageHelper) extends DeviceController {

  def download = Action.async { implicit request =>
    (for {
      req <- deviceGetRequest(ImageDownloadRequest.PARSER)
      images <- imageHelper.loadImages(req.data.getCardsIdsList.asScala.map(Integer2int), req.data.getIncludeFull)
      //      list = images.map { image =>
      //        val b = CardImage.newBuilder()
      //          .setCardId(image.id)
      //          .setThumbnailBytes(ByteString.copyFrom(image.thumbBytes))
      //
      //        if (req.data.getIncludeFull)
      //          b.setFullImageBytes(ByteString.copyFrom(image.bytes))
      //
      //        b.build
      //      }
      resp <- TarGzHelper.compress(images)
    } yield resp).toResponse
  }

  protected def createImageResponse(list: Seq[CardImage]): Future[Array[Byte]] = {
    val data = ImageDownloadResponse.newBuilder()
      .addAllCardsImages(list.asJavaCollection)
      .build().toByteArray

    GZipHelper.compress(data)
  }
}


