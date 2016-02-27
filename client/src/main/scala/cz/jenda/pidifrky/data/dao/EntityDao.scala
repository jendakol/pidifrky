package cz.jenda.pidifrky.data.dao

import android.location.Location
import cz.jenda.pidifrky.data.pojo.{Entity, EntityFactory}
import cz.jenda.pidifrky.data.{CursorWrapper, Database}
import cz.jenda.pidifrky.logic.Application.executionContext
import cz.jenda.pidifrky.logic.DebugReporter
import cz.jenda.pidifrky.logic.exceptions.EntityNotFoundException
import cz.jenda.pidifrky.logic.map.LocationHelper

import scala.collection.SortedSet
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.util.Failure

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait EntityDao[E <: Entity] {
  protected def entityFactory: EntityFactory[E]

  def get(id: Int): Future[E]

  def get(ids: Seq[Int])(implicit ord: Ordering[E]): Future[SortedSet[E]]

  def getAll(implicit ord: Ordering[E]): Future[SortedSet[E]]

  def getNearest(location: Location, perimeter: Double)(implicit ord: Ordering[E]): Future[SortedSet[E]]

  protected def getNearestEntity(location: Location, perimeter: Double)(f: (Double, Double, Double, Double) => String)(implicit ord: Ordering[E], ct: ClassTag[E]): Future[SortedSet[E]] = {
    val range = LocationHelper.toDegrees(perimeter)

    val lat = math.abs(location.getLatitude)
    val lon = math.abs(location.getLongitude)

    toEntityList(Database.rawQuery(f(lat - range, lat + range, lon - range, lon + range)))
  }

  protected def toEntityList(c: Future[CursorWrapper])(implicit ord: Ordering[E], ct: ClassTag[E]): Future[SortedSet[E]] =
    c.map { c =>
      try {
        c.mapToList(entityFactory)
      }
      finally
        c.close()
    }.flatMap(Future.fromTry)
      .recover {
        case EntityNotFoundException(desc) =>
          DebugReporter.debug("No entity was found - " + desc)
          Seq()
      }
      .map(s => collection.SortedSet(s: _*))
      .andThen {
        case Failure(e: Exception) => DebugReporter.debugAndReport(e)
      }
}
