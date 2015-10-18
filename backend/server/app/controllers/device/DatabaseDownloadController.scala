package controllers.device

import javax.inject.Inject

import controllers.device.DeviceControllerImplicits._
import cz.jenda.pidifrky.proto.DeviceBackend.DatabaseUpdateResponse.UpdatedLinks.{CardToMerchants, MerchantToCards}
import cz.jenda.pidifrky.proto.DeviceBackend.DatabaseUpdateResponse.{Card, Location, Merchant, UpdatedLinks}
import cz.jenda.pidifrky.proto.DeviceBackend.{DatabaseUpdateRequest, DatabaseUpdateResponse}
import data.{Card_x_MerchantPojo, Dao, UpdatedTimestamps}
import play.api.mvc.Action
import utils.Format

import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
  * @author Jenda Kolena, kolena@avast.com
  */
class DatabaseDownloadController @Inject()(dao: Dao) extends DeviceController {

  def download = Action.async { implicit request =>
    (for {
      req <- deviceRequest(DatabaseUpdateRequest.PARSER)
      timestamps <- dao.getUpdatedTimestamps
      update <- handleUpdate(req.data, timestamps)
    } yield update).toResponse
  }

  protected[controllers] def handleUpdate(request: DatabaseUpdateRequest, updatedTimestamps: UpdatedTimestamps): Future[DatabaseUpdateResponse] =
    for {
      links <- dao.getAllLinks.map(toMappedLinks)
      newCards <- getNewCards(request.getKnownCardsIdsList.asScala.map(_.toInt), links)
      t = request.getLastUpdate
      merchs <- if (updatedTimestamps.merchants > t) getAllMerchants(links) else Future.successful(Seq())
    } yield {
      val builder = DatabaseUpdateResponse.newBuilder()
        .addAllCards(newCards.asJava)
        .addAllMerchants(merchs.asJava)

      if (updatedTimestamps.links > t) builder.setUpdatedLinks(toUpdatedLinks(links))

      builder.build()
    }


  protected[controllers] def getNewCards(knownCards: Seq[Int], mappedLinks: MappedLinks): Future[Seq[Card]] =
    dao.getUnknownCards(knownCards).map(_.map { card =>
      val builder = Card.newBuilder()

      builder.setId(card.id)
        .setNumber(card.number)
        .setName(card.name)
        .setNameRaw(Format.nameRaw(card.name))

      builder.addAllNeighbours(card.neighboursIds.split(", ?").map(_.trim.toInt).map(int2Integer).toSeq.asJava)

      val loc = for {
        lat <- card.latitude
        lon <- card.longitude
      } yield {
        Location.newBuilder().setLatitude(lat).setLongitude(lon).build()
      }

      loc.foreach(builder.setLocation)

      mappedLinks.cardToMerchants.get(card.id).foreach(ids => builder.addAllMerchantsIds(ids.map(int2Integer).asJava))

      builder.build()
    })

  protected[controllers] def getAllMerchants(mappedLinks: MappedLinks): Future[Seq[Merchant]] = dao.getAllMerchants.map(_.map { merchant =>
    val builder = Merchant.newBuilder()
      .setId(merchant.id)
      .setName(merchant.name)
      .setNameRaw(Format.nameRaw(merchant.name))
      .setAddress(merchant.address)

    val loc = for {
      lat <- merchant.latitude
      lon <- merchant.longitude
    } yield {
      Location.newBuilder().setLatitude(lat).setLongitude(lon).setPrecise(merchant.gps > 0).build()
    }

    loc.foreach(builder.setLocation)

    mappedLinks.merchantToCards.get(merchant.id).foreach(ids => builder.addAllCardsIds(ids.map(int2Integer).asJava))

    builder.build()
  })

  protected[controllers] def toMappedLinks(links: Seq[Card_x_MerchantPojo]): MappedLinks = {
    val merchantToCards = links.groupBy(_.merchantId).mapValues(_.map(_.cardId))
    val cardToMerchants = links.groupBy(_.cardId).mapValues(_.map(_.merchantId))

    MappedLinks(cardToMerchants, merchantToCards)
  }

  protected[controllers] def toUpdatedLinks(links: MappedLinks): UpdatedLinks = {
    val cardToMerchants = links.cardToMerchants.map { case (card, merchants) =>
      CardToMerchants.newBuilder().setCardId(card).addAllMerchantsIds(merchants.map(int2Integer).asJava).build()
    }.asJava

    val merchantToCards = links.merchantToCards.map { case (merchant, cards) =>
      MerchantToCards.newBuilder().setMerchantId(merchant).addAllCardsIds(cards.map(int2Integer).asJava).build()
    }.asJava

    UpdatedLinks.newBuilder()
      .addAllCardMerchantsLinks(cardToMerchants)
      .addAllMerchantCardsLinks(merchantToCards)
      .build()
  }
}

case class MappedLinks(cardToMerchants: Map[Int, Seq[Int]], merchantToCards: Map[Int, Seq[Int]])
