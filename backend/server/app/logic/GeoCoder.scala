package logic

import javax.inject.Inject

import annots.{BlockingExecutor, CallbackExecutor}
import exceptions.InvalidPayloadException
import rapture.core.modes.returnTry._
import rapture.json.jsonBackends.jackson._
import rapture.json.{Json, _}
import utils.{ConfigProperty, Logging}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * @author Jenda Kolena, kolena@avast.com
  */
class GeoCoder @Inject()(@ConfigProperty("url.google.geocoding") googleUrlGeocoding: String, @BlockingExecutor blocking: ExecutionContext, @CallbackExecutor implicit val ec: ExecutionContext) extends Logging {
  def getLocation(address: String): Future[Option[(Float, Float)]] =
    HttpClient.get(googleUrlGeocoding, "address" -> address)
      .map(r => new String(r.payload))
      .map { resp =>
        val json = Json.parse(resp)

        json.map(_.results(0)).map(_.geometry).map(_.location).flatMap {
          case json"""{"lat": $lat, "lng": $lon}""" => Try((lat.as[Float].get, lon.as[Float].get))

          case _ => Failure(InvalidPayloadException("Google", "geocode response"))
        } match {
          case Success((lat: Float, lon: Float)) => Option((lat, lon))
          case Failure(e) =>
            Logger.warn("Error while calling the Geocoding service", e)
            None
        }
      }
}
