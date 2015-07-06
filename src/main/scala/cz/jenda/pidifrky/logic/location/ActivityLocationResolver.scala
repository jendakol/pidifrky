package cz.jenda.pidifrky.logic.location

import com.google.android.gms.location.DetectedActivity
import cz.jenda.pidifrky.logic.DebugReporter
import io.nlopez.smartlocation.location.config.{LocationAccuracy, LocationParams}
import io.nlopez.smartlocation.location.providers.LocationBasedOnActivityProvider.LocationBasedOnActivityListener

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object ActivityLocationResolver extends LocationBasedOnActivityListener {
  override def locationParamsForActivity(detectedActivity: DetectedActivity): LocationParams = {
    import DetectedActivity._

    val baseInterval = LocationHandler.getSetInterval.getOrElse(5000) //TODO: default
    val baseDistance = 100

    DebugReporter.debug("Detected activity " + detectedActivity)

    val coef: Double = detectedActivity.getType match {
      case IN_VEHICLE => 1
      case ON_BICYCLE => 0.75
      case ON_FOOT | WALKING | RUNNING => 0.5
      case STILL => 2
      case _ => 0.75
    }

    val interval = baseInterval * coef
    val distance = baseDistance * coef

    DebugReporter.debug(s"Location settings: interval=$interval, distance=$distance")

    new LocationParams.Builder()
      .setAccuracy(LocationAccuracy.HIGH)
      .setDistance(distance.toInt)
      .setInterval(interval.toInt)
      .build()
  }
}
