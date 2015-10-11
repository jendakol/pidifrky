package data

import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject

import annots.CallbackExecutor
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

/**
 * @author Jenda Kolena, kolena@avast.com
 */
class Dao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider, @CallbackExecutor implicit val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with Logging {

  import driver.api._

  val lock = new ReentrantReadWriteLock()

  private val Cards = TableQuery[CardsTable]
  private val Cards_x_Merchants = TableQuery[Card_x_MerchantTable]
  private val Merchants = TableQuery[MerchantsTable]

  def insertMerchants(merchs: Seq[MerchantPojo]): Future[Unit] = db.run(Merchants ++= merchs).andThen {
    case Failure(t) => Logger.warn(s"Error while inserting merchants into DB", t)
  }.map(_ => ())

  def insertCardsWithLinks(cardsWithLinks: Seq[(CardPojo, Seq[Int])]): Future[Unit] = {
    val map = cardsWithLinks.toMap.map { case (card, ids) =>
      (card, ids.map(id => Card_x_MerchantPojo(None, card.id, id)))
    }

    val cards = map.keys

    val linksFiltered = db.run(Merchants.result).map { merchs =>
      val ids = merchs.map(_.id)

      map.values.flatten
        .filter(r => ids.contains(r.merchantId))
    }

    for {
      _ <- insertCards(cards)
      links <- linksFiltered
      _ <- insertCardMerchantLinks(links)
    } yield ()
  }

  protected def insertCards(cards: Iterable[CardPojo]): Future[Unit] =
    db.run(sql"SELECT id FROM cards".as[Int]).flatMap { existing =>
      val nonExisting = cards.filterNot(c => existing.contains(c.id))

      Logger.debug("Inserting new cards: " + nonExisting.mkString(", "))

      db.run(Cards ++= nonExisting).andThen {
        case Failure(t) => Logger.warn(s"Error while inserting cards into DB", t)
      }
    }.map(_ => ())

  protected def insertCardMerchantLinks(links: Iterable[Card_x_MerchantPojo]): Future[Unit] = db.run(Cards_x_Merchants ++= links).andThen {
    case Failure(t) => Logger.warn(s"Error while inserting card_x_merchant links into DB", t)
  }.map(_ => ())

  def deleteUnusedMerchants(): Future[Unit] = {
    db.run(sqlu"DELETE FROM merchants WHERE (SELECT count(*) FROM cards_x_merchants WHERE cards_x_merchants.merchant_id = merchants.id) = 0").map(_ => ())
  }

  def deleteAllMerchants(): Future[Unit] = {
    db.run(Merchants.delete).map(_ => ())
  }

  def deleteAllCardMerchantLinks(): Future[Unit] = {
    db.run(Cards_x_Merchants.delete).map(_ => ())
  }

  class CardsTable(tag: Tag) extends Table[CardPojo](tag, "cards") {

    def id = column[Int]("id", O.PrimaryKey)

    def number = column[Int]("number")

    def name = column[String]("name")

    def latitude = column[Option[Float]]("latitude")

    def longitude = column[Option[Float]]("longitude")

    def neighbours = column[String]("neighbours")

    def * = (id, number, name, latitude, longitude, neighbours) <>(CardPojo.tupled, CardPojo.unapply)
  }

  class MerchantsTable(tag: Tag) extends Table[MerchantPojo](tag, "merchants") {

    def id = column[Int]("id", O.PrimaryKey)

    def name = column[String]("name")

    def address = column[String]("address")

    def latitude = column[Option[Float]]("latitude")

    def longitude = column[Option[Float]]("longitude")

    def gps = column[Int]("gps")

    def * = (id, name, address, latitude, longitude, gps) <>(MerchantPojo.tupled, MerchantPojo.unapply)
  }

  class Card_x_MerchantTable(tag: Tag) extends Table[Card_x_MerchantPojo](tag, "cards_x_merchants") {

    import slick.driver.MySQLDriver.api._

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def card_id = column[Int]("card_id")

    def merchant_id = column[Int]("merchant_id")

    def card = foreignKey("FK_cards_x_merchants_cards", card_id, Cards)(_.id)

    def merchant = foreignKey("FK_cards_x_merchants_merchants", merchant_id, Merchants)(_.id)

    def * = (id.?, card_id, merchant_id) <>(Card_x_MerchantPojo.tupled, Card_x_MerchantPojo.unapply)

  }

}
