package cz.jenda.pidifrky.data.dao

import android.location.Location
import cz.jenda.pidifrky.MerchantsTable
import cz.jenda.pidifrky.data.Database
import cz.jenda.pidifrky.data.pojo.Merchant
import cz.jenda.pidifrky.logic.Application.executionContext

import scala.collection.SortedSet
import scala.concurrent.Future

/**
 * @author Jenda Kolena, kolena@avast.com
 */
object MerchantsDao extends EntityDao[Merchant] {
  override protected val entityFactory = Merchant

  override def get(id: Int): Future[Merchant] =
    Database.selectFrom(MerchantsTable)(Map("id" -> id), None, None)
      .map(_.mapTo(Merchant))
      .flatMap(Future.fromTry)

  override def get(id: Int, ids: Int*)(implicit ord: Ordering[Merchant]): Future[SortedSet[Merchant]] =
    toEntityList(Database.rawQuery(DbQueries.getMerchants(Seq(id) ++ ids: _*)))

  override def getAll(implicit ord: Ordering[Merchant]): Future[SortedSet[Merchant]] =
    toEntityList(Database.selectFrom(MerchantsTable)(Map(), None, None))

  override def getNearest(location: Location, perimeter: Double)(implicit ord: Ordering[Merchant]): Future[SortedSet[Merchant]] =
    getNearestEntity(location, perimeter)(DbQueries.getNearestMerchants)
}
