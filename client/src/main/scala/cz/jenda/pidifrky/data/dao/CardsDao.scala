package cz.jenda.pidifrky.data.dao

import android.location.Location
import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.data.{CursorWrapper, Database}
import cz.jenda.pidifrky.logic.Application.executionContext
import cz.jenda.pidifrky.logic.DebugReporter
import cz.jenda.pidifrky.logic.exceptions.EntityNotFoundException
import cz.jenda.pidifrky.logic.map.LocationHelper

import scala.collection.SortedSet
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
 * @author Jenda Kolena, kolena@avast.com
 */
object CardsDao extends EntityDao[Card] {

  override def get(id: Int): Future[Card] =
    Database.rawQuery(DbQueries.getCards, Map("id" -> id))
      .map(_.mapTo(Card))
      .flatMap(Future.fromTry)

  override def getAll(implicit ord: Ordering[Card]): Future[SortedSet[Card]] = {
    toCardsList(Database.rawQuery(DbQueries.getCards))
  }

  private var lastNearest: Option[NearestCards] = None

  override def getNearest(location: Location, perimeter: Double)(implicit ord: Ordering[Card]): Future[SortedSet[Card]] =
  //gets cached result (not older than 1500ms) or gets new data from DB
    lastNearest
      .filterNot(l => System.currentTimeMillis() - l.time < 1500)
      .map(n => Future.successful(n.cards))
      .getOrElse {
      val range = LocationHelper.toDegrees(perimeter)

      val lat = math.abs(location.getLatitude)
      val lon = math.abs(location.getLongitude)

      toCardsList(Database.rawQuery(DbQueries.getNearestCards(lat - range, lat + range, lon - range, lon + range))) andThen {
        case Success(cards) => lastNearest = Some(NearestCards(cards, System.currentTimeMillis()))
      }
    }

  private def toCardsList(c: Future[CursorWrapper])(implicit ord: Ordering[Card]): Future[SortedSet[Card]] =
    c.map(_.mapToList(Card))
      .flatMap(Future.fromTry)
      .recover {
      case EntityNotFoundException(desc) => DebugReporter.debug("No entity was found - " + desc)
        Seq()
    }
      .map(s => collection.SortedSet(s: _*))
      .andThen {
      case Failure(e: Exception) => DebugReporter.debugAndReport(e)
    }
}

case class NearestCards(cards: SortedSet[Card], time: Long)
