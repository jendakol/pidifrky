package controllers.device

import javax.inject.Inject

import com.google.protobuf.ByteString
import controllers.device.DeviceControllerImplicits._
import cz.jenda.pidifrky.proto.DeviceBackend.ImageDownloadResponse.CardImage
import cz.jenda.pidifrky.proto.DeviceBackend.{ImageDownloadRequest, ImageDownloadResponse}
import logic.ImageHelper
import play.api.mvc.Action
import utils.helpers.GZipHelper

import scala.collection.JavaConverters._

/**
  * @author Jenda Kolena, kolena@avast.com
  */
class ImageDownloadController @Inject()(imageHelper: ImageHelper) extends DeviceController {

  def download = Action.async { implicit request =>
    deviceRequest(ImageDownloadRequest.PARSER).flatMap { req =>
      imageHelper.loadImages(req.data.getCardsIdsList.asScala.map(Integer2int))
    }.map(_.map { image =>
      CardImage.newBuilder()
        .setCardId(image.id)
        .setFullImageBytes(ByteString.copyFrom(image.bytes))
        .setThumbnailBytes(ByteString.copyFrom(image.thumbBytes))
        .build()
    }).flatMap { list =>
      val data = ImageDownloadResponse.newBuilder()
        .addAllCardsImages(list.asJavaCollection)
        .build().toByteArray

      GZipHelper.compress(data)
    }.toResponse
  }
}


