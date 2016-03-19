package cz.jenda.pidifrky.ui

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.Menu
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.{CameraPosition, LatLng}
import com.google.maps.android.clustering.ClusterManager
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.dao.{CardsDao, MerchantsDao}
import cz.jenda.pidifrky.data.{CardOrdering, MerchantOrdering}
import cz.jenda.pidifrky.logic.FutureImplicits._
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.logic.map._
import cz.jenda.pidifrky.logic.{DebugReporter, PidifrkySettings, Utils}
import cz.jenda.pidifrky.ui.api.{BasicMapActivity, LineOptions, Toast}

import scala.concurrent.Future
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class MapActivity extends BasicMapActivity {

  import MapActivity._

  override protected def actionBarMenu(): Option[Int] = Some(R.menu.map)

  private var viewType: ViewType = ViewType.None //default, to prevent NPE

  private var followLocation: Boolean = true

  private var intent: Option[Intent] = None

  override def onMapReady(map: GoogleMap, intent: Intent, clusterManager: ClusterManager[MapMarker]): Unit = {
    viewType = ViewType(intent.getIntExtra(BundleKeys.ViewType, ViewType.None.id))

    this.intent = Option(intent)

    reloadMapView(true)
    invalidateOptionsMenu()
  }

  private def reloadMapView(forced: Boolean): Unit = {
    viewType match {
      case ViewType.AllCards =>
        import CardOrdering.Implicits.ByName

        withLoadToast(R.string.showing_cards) {
          CardsDao.getAll
            .andThenOnUIThread { case Success(cards) =>
              displayItems(forced, cards)
            }
        }

      case ViewType.NearestCards =>
        import CardOrdering.Implicits.ByName

        LocationHandler.getCurrentLocation.foreach { loc =>
          withLoadToast(R.string.showing_cards) {
            CardsDao.getNearest(loc, PidifrkySettings.closestDistance)
              .andThenOnUIThread { case Success(cards) =>
                displayItems(forced, cards)
              }
          }
        }

      case ViewType.NearestMerchants =>
        import MerchantOrdering.Implicits.ByName

        LocationHandler.getCurrentLocation.foreach { loc =>
          withLoadToast(R.string.showing_merchants) {
            MerchantsDao.getNearest(loc, PidifrkySettings.closestDistance)
              .andThenOnUIThread { case Success(merchs) =>
                displayItems(forced, merchs)
              }
          }
        }

      case ViewType.None =>
        intent.foreach { intent =>
          withLoadToast(R.string.loading_map) {

            //load specific cards
            val cf = Option(intent.getIntArrayExtra(BundleKeys.CardsIds)).map { ids =>
              import CardOrdering.Implicits.ByName

              CardsDao.get(ids.toSeq).mapOnUIThread { cards =>
                cards.toSeq
              }
            }.getOrElse(Future.successful(Seq()))

            //load specific merchants
            val mf = Option(intent.getIntArrayExtra(BundleKeys.MerchantsIds)).map { ids =>
              import MerchantOrdering.Implicits.ByName

              MerchantsDao.get(ids.toSeq).mapOnUIThread { merchs =>
                merchs.toSeq
              }
            }.getOrElse(Future.successful(Seq()))

            for {
              cards <- cf
              merchs <- mf
            } yield Utils.runOnUiThread {
              Option(intent.getParcelableExtra[LatLng](BundleKeys.ShowLineTo)).foreach { latLng =>
                setDistanceLine(LineOptions(Color.RED, 5), latLng)
              }

              displayItems(forced, cards ++ merchs, Option(intent.getParcelableExtra[LatLng](BundleKeys.ShowLineTo)))
            }
          }.andThen { case Failure(NonFatal(e)) =>
            DebugReporter.debugAndReport(e)
          }
        }
    }
  }

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    followLocation = PidifrkySettings.followLocationOnMap
  }

  override def onCameraChange(cameraPosition: CameraPosition): Unit = {
    LocationHandler.getCurrentLocation.foreach { location =>
      if (followLocation) {
        if (!isVisibleOnMap(location)) {
          followLocation = false

          PidifrkySettings.withEditor(_.putBoolean("mapFollowPosition", followLocation))
          Toast(R.string.map_following_off, Toast.Short)
          runOnUiThread(invalidateOptionsMenu())
        }
      }
    }
  }

  override protected def onLocationChanged(location: Location): Unit = {
    super.onLocationChanged(location)

    if (followLocation) {
      centerMap(LocationHelper.toLatLng(location))
    }

    reloadMapView(false)
  }

  override def onPrepareOptionsMenu(menu: Menu): Boolean = {
    import MenuKeys._
    import ViewType._

    Option(menu.findItem(GpsOn)).foreach(_.setVisible(mockLocation))
    Option(menu.findItem(FollowLocation)).foreach(_.setChecked(followLocation))
    Option(menu.findItem(SwitchToList)).foreach(_.setVisible {
      viewType match {
        case NearestCards | AllCards | NearestMerchants => true

        case _ => false
      }
    })

    true
  }

  override protected def onActionBarClicked: PartialFunction[Int, Unit] = {
    case MenuKeys.FollowLocation =>
      followLocation = !followLocation
      PidifrkySettings.withEditor(_.putBoolean("mapFollowPosition", followLocation))

      invalidateOptionsMenu()

      if (followLocation) {
        centerMapToCurrent()
        Toast(R.string.map_following_on, Toast.Short)
      }
      else {
        Toast(R.string.map_following_off, Toast.Short)
      }

    case MenuKeys.GpsOn =>
      LocationHandler.disableMocking
      mockLocation = false
      invalidateOptionsMenu()

    case MenuKeys.ShowNormal =>
      setMapType(NormalMapType)

    case MenuKeys.ShowSatellite =>
      setMapType(SatelliteMapType)

    case MenuKeys.ShowHybrid =>
      setMapType(HybridMapType)

    case MenuKeys.SwitchToList =>
      goWithParamsTo(classOf[ListActivity]) { i =>
        i.putExtra(BundleKeys.ViewType, viewType.id)
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
      }

    case MenuKeys.ReloadDisplay =>
      reloadMapView(true)

    case _ =>
      Toast("Not supported", Toast.Long)
  }

  override protected def upButtonClicked(): Unit = {
    this.finish() //go back!
  }

  override def onMapLongClick(latLng: LatLng): Unit = {
    //TODO mocking on map
    LocationHandler.mockLocation(latLng)
    mockLocation = true
    invalidateOptionsMenu()
  }

  override def onMapMarkerClick(m: MapMarker): Unit = {
    Toast(m.title, Toast.Short)
  }
}

object MapActivity {

  object BundleKeys {
    private val prefix = getClass.getName + "_"

    final val CardsIds = prefix + "cardsIds"
    final val MerchantsIds = prefix + "merchsIds"
    final val ShowLineTo = prefix + "showLineTo"
    final val ViewType = prefix + "viewType"
  }

  private[MapActivity] object MenuKeys {

    import R.id._

    final val GpsOn = menu_map_gpsOn
    final val FollowLocation = menu_map_followLocation
    final val ShowNormal = menu_map_showNormal
    final val ShowSatellite = menu_map_showSatellite
    final val ShowHybrid = menu_map_showHybrid
    final val ReloadDisplay = menu_map_reloadDisplay
    final val ShowSettings = menu_map_showSettings
    final val ShowHelp = menu_map_showHelp
    final val SwitchToList = menu_map_switchToList
  }

  sealed abstract class ViewType(val id: Int)

  object ViewType {
    def apply(id: Int): ViewType = id match {
      case None.id => None
      case AllCards.id => AllCards
      case NearestCards.id => NearestCards
      case NearestMerchants.id => NearestMerchants
    }

    case object None extends ViewType(0)

    case object AllCards extends ViewType(1)

    case object NearestCards extends ViewType(2)

    case object NearestMerchants extends ViewType(3)

  }


}
