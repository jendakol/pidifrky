package cz.jenda.pidifrky.data.dao

import android.location.Location
import cz.jenda.pidifrky.data.{MerchantsTable, Database}
import cz.jenda.pidifrky.data.pojo.Merchant
import cz.jenda.pidifrky.logic.Application.executionContext

import scala.collection.SortedSet
import scala.concurrent.Future

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object MerchantsDao extends EntityDao[Merchant] {
  override protected val entityFactory = Merchant

  override def get(id: Int): Future[Merchant] =
    Database.selectFrom(MerchantsTable)(Map("id" -> id), None, None)
      .map(_.mapTo(Merchant))
      .flatMap(Future.fromTry)

  override def get(ids: Seq[Int])(implicit ord: Ordering[Merchant]): Future[SortedSet[Merchant]] =
    toEntityList(Database.rawQuery(DbQueries.getMerchants(ids: _*)))

  override def getAll(implicit ord: Ordering[Merchant]): Future[SortedSet[Merchant]] =
    toEntityList(Database.rawQuery(DbQueries.getMerchants()))

  override def getNearest(location: Location, perimeter: Double)(implicit ord: Ordering[Merchant]): Future[SortedSet[Merchant]] =
    getNearestEntity(location, perimeter)(DbQueries.getNearestMerchants)
}
