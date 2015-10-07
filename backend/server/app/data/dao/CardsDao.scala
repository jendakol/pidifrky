package data.dao

import javax.inject.Inject

import annots.CallbackExecutor
import data.CardPojo
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

/**
 * @author Jenda Kolena, kolena@avast.com
 */
class CardsDao @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider, @CallbackExecutor implicit val ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import driver.api._

  private val Cards = TableQuery[CardsTable]

  def insert(card: CardPojo): Future[Unit] = db.run(Cards += card).map(_ => ())

  def all(): Future[Seq[CardPojo]] = db.run(Cards.result)

  class CardsTable(tag: Tag) extends Table[CardPojo](tag, "cards") {

    def id = column[Int]("id", O.PrimaryKey)

    def number = column[Int]("number")

    def name = column[String]("number")

    def latitude = column[Option[Float]]("latitude")

    def longitude = column[Option[Float]]("longitude")

    def neighbours = column[String]("neighbours")

    def * = (id, number, name, latitude, longitude, neighbours) <>(CardPojo.tupled, CardPojo.unapply)
  }

}
