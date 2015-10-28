package cz.jenda.pidifrky.data.dao

import android.location.Location
import cz.jenda.pidifrky.data.Database
import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.logic.Application.executionContext

import scala.collection.SortedSet
import scala.concurrent.Future
import scala.util.Success

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object CardsDao extends EntityDao[Card] {
  override protected val entityFactory = Card

  override def get(id: Int): Future[Card] =
    Database.rawQuery(DbQueries.getCards, Map("id" -> id))
      .map(_.mapTo(Card))
      .flatMap(Future.fromTry)

  override def get(id: Int, ids: Int*)(implicit ord: Ordering[Card]): Future[SortedSet[Card]] =
    toEntityList(Database.rawQuery(DbQueries.getCards(Seq(id) ++ ids: _*)))

  override def getAll(implicit ord: Ordering[Card]): Future[SortedSet[Card]] =
    toEntityList(Database.rawQuery(DbQueries.getCards))

  def getAllIds: Future[Seq[Int]] =
    Database.rawQuery(DbQueries.getAllCardsIds).flatMap(s => Future.fromTry(s.map(_.getInt(0))))

  private var lastNearest: Option[NearestCards] = None

  override def getNearest(location: Location, perimeter: Double)(implicit ord: Ordering[Card]): Future[SortedSet[Card]] =
  //gets cached result (not older than 1500ms) or gets new data from DB
    lastNearest
      .filterNot(l => System.currentTimeMillis() - l.time < 1500)
      .map(n => Future.successful(n.cards))
      .getOrElse {

        getNearestEntity(location, perimeter)(DbQueries.getNearestCards) andThen {
          case Success(cards) => lastNearest = Some(NearestCards(cards, System.currentTimeMillis()))
        }
      }
}

case class NearestCards(cards: SortedSet[Card], time: Long)
