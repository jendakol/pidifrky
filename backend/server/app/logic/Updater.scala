package logic

import javax.inject.Inject

import annots.{BlockingExecutor, CallbackExecutor}
import data.{CardPojo, Dao, MerchantPojo}
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.xml.Elem

/**
 * @author Jenda Kolena, kolena@avast.com
 */
class Updater @Inject()(dao: Dao, @BlockingExecutor blocking: ExecutionContext, @CallbackExecutor implicit val ec: ExecutionContext) extends Logging {
  protected final val Gps = "(\\d{1,2}\\.\\d+), ?(\\d{1,2}\\.\\d+)".r

  def update(): Future[Unit] = Future {
    //TODO - download

    import scala.xml.XML

    XML.load(Updater.this.getClass.getClassLoader.getResourceAsStream("temp/android.xml"))

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

      val (lat, lon) = extractGps(gps)

      (CardPojo(id, number, name, lat, lon, neighbours), merchantIds)
    }
  }(blocking)

  protected def parseMerchants(xml: Elem): Future[Seq[MerchantPojo]] = Future {
    (xml \\ "seznamprodejcu" \\ "prodejce").toSeq.map { merchant =>
      val id = (merchant \\ "@id").text.toInt
      val name = merchant.text

      val address = (merchant \\ "@adresa").text

      val gps = (merchant \\ "@gps").text

      val (lat, lon) = extractGps(gps)

      //TODO - try to get GPS from address if it's not included

      MerchantPojo(id, name, address, lat, lon, 1)
    }
  }(blocking)

  protected def extractGps(gps: String): (Option[Float], Option[Float]) = gps match {
    case Gps(la, lo) => try {
      (Some(la.toFloat), Some(lo.toFloat))
    }
    catch {
      case e: Exception =>
        Logger.warn("Error while parsing GPS")
        (None, None)
    }

    case _ => (None, None)
  }
}
