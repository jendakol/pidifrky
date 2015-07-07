package cz.jenda.pidifrky.logic.location

import com.google.android.gms.location.DetectedActivity
import cz.jenda.pidifrky.logic.{DebugReporter, PidifrkySettings}
import io.nlopez.smartlocation.location.config.{LocationAccuracy, LocationParams}
import io.nlopez.smartlocation.location.providers.LocationBasedOnActivityProvider.LocationBasedOnActivityListener

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object ActivityLocationResolver extends LocationBasedOnActivityListener {
  //TODO: default values
  protected final val BaseDistance = 80

  protected final val DefaultCoef = 1

  protected var lastActivity: Option[DetectedActivity] = None

  private var baseInterval = PidifrkySettings.gpsUpdateInterval

  override def locationParamsForActivity(detectedActivity: DetectedActivity): LocationParams = {

    val changed = lastActivity match {
      //log only change
      case Some(a) if !activitiesEquals(a, detectedActivity) =>
        GpsLogger.addEvent(detectedActivity.toString)
        lastActivity = Some(detectedActivity)
        true
      case None =>
        GpsLogger.addEvent(detectedActivity.toString)
        lastActivity = Some(detectedActivity)
        true
      case _ =>
        DebugReporter.debug(detectedActivity.toString) // only debug log
        false
    }

    val coef: Double = if (detectedActivity.getConfidence <= 30) {
      DefaultCoef
    }
    else {
      import DetectedActivity._

      detectedActivity.getType match {
        case IN_VEHICLE => 1
        case ON_BICYCLE => 0.5
        case ON_FOOT | WALKING | RUNNING => 0.3
        case STILL => 1.2
        case _ => DefaultCoef
      }
    }

    val interval = baseInterval * coef
    val distance = BaseDistance * coef

    if (changed) DebugReporter.debug(s"Location settings: interval=$interval, distance=$distance")

    new LocationParams.Builder()
      .setAccuracy(LocationAccuracy.HIGH)
      .setDistance(distance.toInt)
      .setInterval(interval.toInt)
      .build()
  }

  def setBaseInterval(interval: Int): Unit = baseInterval = interval

  private def activitiesEquals(ac1: DetectedActivity, ac2: DetectedActivity): Boolean = {
    ac1.getType == ac2.getType && ac1.getConfidence == ac2.getConfidence
  }
}
