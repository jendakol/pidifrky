package logic

import javax.inject.Inject

import annots.{BlockingExecutor, CallbackExecutor}
import data.{CardPojo, Dao, MerchantPojo}
import utils.{ConfigProperty, Logging}

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.{Elem, XML}

/**
  * @author Jenda Kolena, kolena@avast.com
  */
class Updater @Inject()(dao: Dao, @ConfigProperty("url.pidifrk.xml") xmlDataUrl: String, geoCoder: GeoCoder, @BlockingExecutor blocking: ExecutionContext, @CallbackExecutor implicit val ec: ExecutionContext) extends Logging {
  protected final val Gps = "(\\d{1,2}\\.\\d+), ?(\\d{1,2}\\.\\d+)".r

  def update(): Future[Unit] = HttpClient.get(xmlDataUrl).map { resp =>
    XML.load(resp.asStream)
  }(blocking) flatMap { xml =>
    for {
      merchants <- parseMerchants(xml)
      cards <- parseCards(xml)
      _ <- performUpdate(merchants, cards)
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
    (xml \\ "pidifrky" \\ "pidifrk").toSeq.map { card =>
      val id = (card \\ "@id").text.toInt
      val number = (card \\ "@cislo").text.toInt
      val name = (card \\ "nazev").text
      val neighbours = (card \\ "tabulka").text

      val merchantIds = (card \\ "prodejci" \\ "id").toSeq.map(_.text.toInt)

      val gps = (card \\ "gps").text

      val (lat, lon) = extractGps(gps).map { case (la, lo) => (Option(la), Option(lo)) }.getOrElse((None, None))

      //TODO: guess location for cards

      (CardPojo(id, number, name, lat, lon, neighbours), merchantIds)
    }
    //TODO: filter ??? etc.
  }(blocking)

  protected def parseMerchants(xml: Elem): Future[Seq[MerchantPojo]] = Future {
    (xml \\ "seznamprodejcu" \\ "prodejce").toSeq.map { merchant =>
      val id = (merchant \\ "@id").text.toInt
      val name = merchant.text

      val address = (merchant \\ "@adresa").text

      val gps = (merchant \\ "@gps").text

      extractGps(gps) match {
        case Some((lat, lon)) => Future.successful(MerchantPojo(id, name, address, Option(lat), Option(lon), gps = 1))
        case None => geoCoder.getLocation(address).map {
          case Some((lat, lon)) => MerchantPojo(id, name, address, Option(lat), Option(lon), gps = 0)
          case None => MerchantPojo(id, name, address, None, None, gps = 0)
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
