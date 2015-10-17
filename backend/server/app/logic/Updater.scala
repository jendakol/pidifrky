package logic

import javax.inject.Inject

import annots.{ConfigProperty, BlockingExecutor, CallbackExecutor}
import data.{CardPojo, Dao, MerchantPojo, PojoFactory}
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, XML}

/**
  * @author Jenda Kolena, kolena@avast.com
  */
class Updater @Inject()(dao: Dao, imageHelper: ImageHelper, @ConfigProperty("url.pidifrk.xml") xmlDataUrl: String, geoCoder: GeoCoder, @BlockingExecutor blocking: ExecutionContext, @CallbackExecutor implicit val ec: ExecutionContext) extends Logging {
  protected final val Gps = "(\\d{1,2}\\.\\d+), ?(\\d{1,2}\\.\\d+)".r

  def update(): Future[Unit] = HttpClient.get(xmlDataUrl).map { resp =>
    XML.load(resp.stream)
  }(blocking) flatMap { xml =>
    val merchantsF = parseMerchants(xml)
    val cardsF = parseCards(xml)

    val imagesF = cardsF.map(_.map(_._1.number)).flatMap(imageHelper.downloadImagesForCards)

    for {
      merchants <- merchantsF
      cards <- cardsF
      _ <- performUpdate(merchants, cards)
      _ <- imagesF
    } yield ()
  }

  protected def performUpdate(merchants: Seq[MerchantPojo], cards: Seq[(CardPojo, Seq[Int])]): Future[Unit] = DataLock.withWriteLock {
    for {
      _ <- dao.deleteAllMerchants()
      _ <- dao.insertMerchants(merchants)
      _ <- dao.deleteAllCardMerchantLinks()
      _ <- dao.insertCardsWithLinks(cards)
      _ <- dao.deleteUnusedMerchants()
      _ <- dao.updateHashes()
    } yield ()
  }

  protected def parseCards(xml: Elem): Future[Seq[(CardPojo, Seq[Int])]] = Future {
    (xml \\ "pidifrky" \\ "pidifrk").toSeq.flatMap { card =>
      val id = (card \\ "@id").text.toInt
      val number = (card \\ "@cislo").text.toInt
      val name = (card \\ "nazev").text
      val neighbours = (card \\ "tabulka").text

      val merchantIds = (card \\ "prodejci" \\ "id").toSeq.map(_.text.toInt)

      val gps = (card \\ "gps").text

      val loc = extractGps(gps).orElse(UpdateHelper.positionForCard(id))

      if (name == "???")
        None
      else
        Some((PojoFactory.createCard(id, number, name, loc, neighbours), merchantIds))
    }
  }(blocking)

  protected def parseMerchants(xml: Elem): Future[Seq[MerchantPojo]] = Future {
    (xml \\ "seznamprodejcu" \\ "prodejce").toSeq.map { merchant =>
      val id = (merchant \\ "@id").text.toInt
      val name = merchant.text

      val address = UpdateHelper.fixAddress((merchant \\ "@adresa").text)

      val gps = (merchant \\ "@gps").text

      extractGps(gps).orElse(UpdateHelper.positionForMerchant(id)) match {
        case Some((lat, lon)) => Future.successful(PojoFactory.createMerchant(id, name, address, Some(lat, lon), precise = true))
        case None =>
          geoCoder.getLocation(address).map { loc =>
            PojoFactory.createMerchant(id, name, address, loc, precise = false)
          }
      }
    }
  }(blocking).flatMap(Future.sequence(_))

  protected def extractGps(gps: String): Option[(Float, Float)] = gps match {
    case Gps(la, lo) => try {
      Some(la.toFloat, lo.toFloat)
    }
    catch {
      case e: Exception =>
        Logger.warn("Error while parsing GPS")
        None
    }

    case _ => None
  }
}
