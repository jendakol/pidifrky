package cz.jenda.pidifrky.logic.dbimport

import cz.jenda.pidifrky.data.Database
import cz.jenda.pidifrky.data.dao._
import cz.jenda.pidifrky.data.pojo.{Card, CardState, Merchant, MerchantLocation}
import cz.jenda.pidifrky.logic.http.HttpRequester
import cz.jenda.pidifrky.logic.{PidifrkySettings, ProgressListener, Transaction, Utils}
import cz.jenda.pidifrky.proto.DeviceBackend.{DatabaseUpdateRequest, DatabaseUpdateResponse}

import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
 * @author Jenda Kolena, kolena@avast.com
 */
object DbImporter {

  import cz.jenda.pidifrky.logic.Application._

  def update(progressListener: ProgressListener): Future[Unit] = withOnlineStatus {
    Transaction.async("database-update") {
      progressListener(0)

      CardsDao.getAllIds.map { ids =>
        progressListener(10)
        DatabaseUpdateRequest.newBuilder()
          .setLastUpdate(PidifrkySettings.lastDatabaseUpdateTimestamp)
          .addAllKnownCardsIds(ids.map(int2Integer).asJava)
          .build()
      }.flatMap(HttpRequester.databaseUpdate).flatMap { resp =>
        progressListener(20)

        val cards = getCards(resp)
        progressListener(30)

        val merchants = getMerchants(resp)
        progressListener(40)

        /* -- to avoid updating just inserted items: */

        val cardToMerchantsLinks = getCardToMerchantsLinks(resp) -- cards.map(_.id)
        val merchantToCardsLinks = getMerchantToCardsLinks(resp) -- merchants.map(_.id)

        progressListener(60)

        val builder = Seq.newBuilder[InsertCommand]

        builder ++= cards.map(CardInsertCommand)
        builder ++= merchants.map(MerchantInsertCommand)

        progressListener(70)

        builder ++= cardToMerchantsLinks.map { case (cardId, merchantsIds) => CardToMerchantsLinkUpdateCommand(cardId, merchantsIds) }
        builder ++= merchantToCardsLinks.map { case (merchantId, cardsIds) => MerchantToCardsLinkUpdateCommand(merchantId, cardsIds) }

        val commands = builder.result()

        progressListener(80)

        Database.executeTransactionally(commands).andThen {
          case _ => progressListener(100)
        }
      }
    }
  }

  protected def getCardToMerchantsLinks(resp: DatabaseUpdateResponse): Map[Int, Seq[Int]] = {
    resp.getUpdatedLinks.getCardMerchantsLinksList.asScala
      .map(l => (l.getCardId, l.getMerchantsIdsList.asScala.map(Integer2int))).toMap
  }

  protected def getMerchantToCardsLinks(resp: DatabaseUpdateResponse): Map[Int, Seq[Int]] = {
    resp.getUpdatedLinks.getMerchantCardsLinksList.asScala
      .map(l => (l.getMerchantId, l.getCardsIdsList.asScala.map(Integer2int))).toMap
  }

  protected def getCards(resp: DatabaseUpdateResponse): Seq[Card] = resp.getCardsList.asScala.map { c =>
    val loc = if (c.hasLocation) {
      val l = c.getLocation
      Some(Utils.toLocation(l.getLatitude, l.getLongitude))
    } else None

    val merchants = c.getMerchantsIdsList.asScala.map(Integer2int)

    val neighbours = c.getNeighboursList.asScala.map(i => if (i > 0) Some(i.toInt) else None)

    Card(c.getId, c.getNumber, c.getName, c.getNameRaw, CardState.NONE, loc, hasImage = false, neighbours, merchants)
  }

  protected def getMerchants(resp: DatabaseUpdateResponse): Seq[Merchant] = resp.getMerchantsList.asScala.map { m =>
    val loc = {
      val l = m.getLocation

      MerchantLocation(
        if (m.hasLocation) Some(Utils.toLocation(l.getLatitude, l.getLongitude)) else None,
        m.getLocation.getPrecise)
    }

    val cards = m.getCardsIdsList.asScala.map(Integer2int)

    Merchant(m.getId, m.getName, m.getNameRaw, m.getAddress, loc, cards)
  }
}