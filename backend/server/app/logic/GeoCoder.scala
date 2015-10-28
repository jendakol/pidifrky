package logic

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import annots.{BlockingExecutor, CallbackExecutor, ConfigProperty}
import com.google.common.util.concurrent.RateLimiter
import exceptions.InvalidPayloadException
import rapture.core.modes.returnTry._
import rapture.json.jsonBackends.jackson._
import rapture.json.{Json, _}
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * @author Jenda Kolena, jendakolena@gmail.com
  */
class GeoCoder @Inject()(@ConfigProperty("url.google.geocoding") googleUrlGeocoding: String, @ConfigProperty("keys.google.geocoding") googleGeocodingKey: String, @BlockingExecutor blocking: ExecutionContext, @CallbackExecutor implicit val ec: ExecutionContext) extends Logging {
  protected val limiter = RateLimiter.create(10, 60, TimeUnit.SECONDS)
  protected val lock = new Semaphore(10)

  def getLocation(address: String): Future[Option[(Float, Float)]] =
    lock.withLockAsync {
      Future {
        limiter.tryAcquire(15, TimeUnit.SECONDS)
      }(blocking).flatMap { acquired =>
        if (!acquired) {
          Logger.error("Couldn't start the Geocoding request in 5 s")
          Future.successful(None)
        }
        else {
          HttpClient.get(googleUrlGeocoding, "address" -> address, "key" -> googleGeocodingKey)
            .map { resp =>
              val json = Json.parse(resp.asString)

              json.map(_.status).flatMap(_.as[String]) match {
                case Success(status: String) => status match {
                  case "OK" => extractValidResponse(json)
                  case "ZERO_RESULTS" =>
                    Logger.info(s"No result was found for address '$address'")
                    None
                  case "OVER_QUERY_LIMIT" =>
                    Logger.warn(s"Limit for Geocoding service was exceeded")
                    None
                  case _ =>
                    Logger.warn(s"Unknown status of Geocoding API: '$status'")
                    None
                }

                case Failure(e) =>
                  Logger.warn("Error while calling the Geocoding service", e)
                  None
              }
            }
        }
      }
    }

  protected def extractValidResponse(json: Try[Json]): Option[(Float, Float)] =
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
