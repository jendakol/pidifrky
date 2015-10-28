package controllers.device

import java.util.UUID

import cz.jenda.pidifrky.proto.DeviceBackend.Envelope.DeviceInfo
import cz.jenda.pidifrky.proto.DeviceBackend.{DatabaseUpdateRequest, DatabaseUpdateResponse, Envelope}
import data._
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.FunSuite
import org.scalatest.mock.MockitoSugar
import org.specs2.runner.JUnitRunner
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.TestImplicits._

import scala.collection.JavaConverters._
import scala.concurrent.Future

/**
  * @author Jenda Kolena, jendakolena@gmail.com
  */
@RunWith(classOf[JUnitRunner])
class DatabaseDownloadControllerTest extends FunSuite with MockitoSugar {

  object DB {
    val cards = Seq(
      CardPojo(1, 1, "name1", Some(50), Some(14), "1,2,3,4,5"),
      CardPojo(2, 2, "name2", Some(50), Some(14), "2,3,4,5,1"),
      CardPojo(3, 3, "name3", Some(50), Some(14), "3,4,5,1,2"),
      CardPojo(4, 4, "name4", Some(50), Some(14), "4,5,1,2,3"),
      CardPojo(5, 5, "name5", Some(50), Some(14), "5,1,2,3,4")
    )

    val merchants = Seq(
      MerchantPojo(1, "merch1", "addr1", Some(50), Some(14), 1),
      MerchantPojo(2, "merch2", "addr2", Some(50), Some(14), 1),
      MerchantPojo(3, "merch3", "addr3", Some(50), Some(14), 0),
      MerchantPojo(4, "merch4", "addr4", None, None, 0),
      MerchantPojo(5, "merch5", "addr5", Some(50), Some(14), 0)
    )

    val links = Seq(
      Card_x_MerchantPojo(Some(1), 1, 1),
      Card_x_MerchantPojo(Some(2), 1, 2),
      Card_x_MerchantPojo(Some(3), 1, 3),
      Card_x_MerchantPojo(Some(3), 1, 4),
      Card_x_MerchantPojo(Some(3), 1, 5),
      Card_x_MerchantPojo(Some(4), 2, 2),
      Card_x_MerchantPojo(Some(5), 2, 3),
      Card_x_MerchantPojo(Some(5), 2, 4),
      Card_x_MerchantPojo(Some(5), 2, 5),
      Card_x_MerchantPojo(Some(7), 3, 3),
      Card_x_MerchantPojo(Some(7), 3, 4),
      Card_x_MerchantPojo(Some(7), 3, 5),
      Card_x_MerchantPojo(Some(8), 4, 4),
      Card_x_MerchantPojo(Some(8), 4, 5),
      Card_x_MerchantPojo(Some(8), 5, 5),
      Card_x_MerchantPojo(Some(8), 5, 1)
    )
  }

  val lastUpdateMerchants = 100000
  val lastUpdateLinks = 200000

  val dao = mock[Dao]
  when(dao.getUpdatedTimestamps).thenReturn(Future.successful(UpdatedTimestamps(lastUpdateMerchants, lastUpdateLinks)))
  when(dao.getAllLinks).thenReturn(Future.successful(DB.links))
  when(dao.getAllMerchants).thenReturn(Future.successful(DB.merchants))

  val controller = new DatabaseDownloadController(dao)

  test("mapping of links") {
    val mapped = controller.toMappedLinks(DB.links)

    assertResult(Map(
      1 -> (1 to 5).toSeq,
      2 -> (2 to 5).toSeq,
      3 -> (3 to 5).toSeq,
      4 -> (4 to 5).toSeq,
      5 -> Seq(5, 1)
    ))(mapped.cardToMerchants)

    assertResult(Map(
      1 -> Seq(1, 5),
      2 -> (1 to 2).toSeq,
      3 -> (1 to 3).toSeq,
      4 -> (1 to 4).toSeq,
      5 -> (1 to 5).toSeq
    ))(mapped.merchantToCards)
  }

  test("getting all merchants") {
    val mapped = controller.toMappedLinks(DB.links)

    val merchantToCards = mapped.merchantToCards

    val merchs = controller.getAllMerchants(mapped).block

    assertResult(Seq(1, 2, 3, 4, 5))(merchs.map(_.getId))

    merchs.foreach { merch =>
      merch.getId match {
        case 1 => assertResult(Set(1, 5))(merch.getCardsIdsList.asScala.toSet.map(Integer2int))
        case 2 => assertResult(Set(1, 2))(merch.getCardsIdsList.asScala.toSet.map(Integer2int))
        case 3 => assertResult((1 to 3).toSet)(merch.getCardsIdsList.asScala.toSet.map(Integer2int))
        case 4 => assertResult((1 to 4).toSet)(merch.getCardsIdsList.asScala.toSet.map(Integer2int))
        case 5 => assertResult((1 to 5).toSet)(merch.getCardsIdsList.asScala.toSet.map(Integer2int))
      }
    }
  }

  test("pure new device request") {
    when(dao.getUnknownCards(Seq())).thenReturn(Future.successful(DB.cards))

    val data = Envelope.newBuilder()
      .setUuid(UUID.randomUUID().toString)
      .setAppVersion("2.0")
      .setDeviceInfo(DeviceInfo.getDefaultInstance)
      .setData(
        DatabaseUpdateRequest.newBuilder()
          //no card IDs
          .setLastUpdate(0)
          .build().toByteString)
      .build().toByteArray

    val request = FakeRequest("POST", "/device/updateDatabase").withRawBody(data)

    val result = controller.download()(request)

    val resp = DatabaseUpdateResponse.parseFrom(contentAsBytes(result))

    val updatedLinks = resp.getUpdatedLinks

    assertResult(5)(resp.getCardsCount)
    assertResult(5)(resp.getMerchantsCount)

    assertResult(5)(updatedLinks.getCardMerchantsLinksCount)
    assertResult(5)(updatedLinks.getMerchantCardsLinksCount)
  }

  test("outdated device request") {
    when(dao.getUnknownCards(Seq(1, 2))).thenReturn(Future.successful(DB.cards.filterNot(_.id <= 2)))

    val data = Envelope.newBuilder()
      .setUuid(UUID.randomUUID().toString)
      .setAppVersion("2.0")
      .setDeviceInfo(DeviceInfo.getDefaultInstance)
      .setData(
        DatabaseUpdateRequest.newBuilder()
          .addAllKnownCardsIds(Seq(1, 2).map(int2Integer).asJava)
          .setLastUpdate((lastUpdateLinks + lastUpdateMerchants) / 2) //should send links
          .build().toByteString)
      .build().toByteArray

    val request = FakeRequest("POST", "/device/updateDatabase").withRawBody(data)

    val result = controller.download()(request)

    val resp = DatabaseUpdateResponse.parseFrom(contentAsBytes(result))

    val updatedLinks = resp.getUpdatedLinks

    assertResult(3)(resp.getCardsCount)
    assertResult(0)(resp.getMerchantsCount)

    assertResult(5)(updatedLinks.getCardMerchantsLinksCount)
    assertResult(5)(updatedLinks.getMerchantCardsLinksCount)
  }
}
