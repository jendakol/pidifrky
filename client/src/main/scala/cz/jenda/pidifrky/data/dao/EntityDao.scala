package cz.jenda.pidifrky.data.dao

import android.location.Location
import cz.jenda.pidifrky.data.pojo.Entity

import scala.collection.SortedSet
import scala.concurrent.Future

/**
 * @author Jenda Kolena, kolena@avast.com
 */
trait EntityDao[E <: Entity] {
  def get(id: Int): Future[E]

  def getAll(implicit ord: Ordering[E]): Future[SortedSet[E]]

  def getNearest(location: Location, perimeter: Double)(implicit ord: Ordering[E]): Future[SortedSet[E]]
}
