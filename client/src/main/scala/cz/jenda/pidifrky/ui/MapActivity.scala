package cz.jenda.pidifrky.ui

import android.content.Intent
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
import cz.jenda.pidifrky.logic.PidifrkySettings
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.logic.map._
import cz.jenda.pidifrky.ui.api.{BasicMapActivity, Toast}

import scala.util.Success

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class MapActivity extends BasicMapActivity {

  import MapActivity._
  import ViewType._

  override protected def actionBarMenu(): Option[Int] = Some(R.menu.map)

  private var viewType: ViewType = NearestCards //default, to prevent NPE

  private var followLocation: Boolean = true

  private var cameraMoved = false

  override def onMapReady(map: GoogleMap, intent: Intent, clusterManager: ClusterManager[MapMarker]): Unit = {
    //load specific cards
    Option(intent.getIntArrayExtra(BundleKeys.CardsIds)).foreach { ids =>
      import CardOrdering.Implicits.ByName

      withLoadToast(R.string.showing_cards) {
        CardsDao.get(ids.toSeq).andThenOnUIThread { case Success(cards) =>
          addMarkers(cards)
        }
      }
    }

    //load specific merchants
    Option(intent.getIntArrayExtra(BundleKeys.MerchantsIds)).foreach { ids =>
      import MerchantOrdering.Implicits.ByName

      withLoadToast(R.string.showing_merchants) {
        MerchantsDao.get(ids.toSeq).andThenOnUIThread { case Success(merchs) =>
          addMarkers(merchs)
        }
      }
    }

    centerMapToCurrent()

    viewType = ViewType(intent.getIntExtra(BundleKeys.ViewType, NearestCards.id))

    showItems()

    invalidateOptionsMenu()
  }

  private def showItems(): Unit = {
    clearMap()

    viewType match {
      case AllCards =>
        import CardOrdering.Implicits.ByName

        withLoadToast(R.string.showing_cards) {
          CardsDao.getAll
            .andThenOnUIThread { case Success(cards) =>
              addMarkers(cards)
            }
        }

      case NearestCards =>
        import CardOrdering.Implicits.ByName

        LocationHandler.getCurrentLocation.foreach { loc =>
          withLoadToast(R.string.showing_cards) {
            CardsDao.getNearest(loc, PidifrkySettings.closestDistance)
              .andThenOnUIThread { case Success(cards) =>
                addMarkers(cards)
              }
          }
        }

      case NearestMerchants =>
        import MerchantOrdering.Implicits.ByName

        LocationHandler.getCurrentLocation.foreach { loc =>
          withLoadToast(R.string.showing_merchants) {
            MerchantsDao.getNearest(loc, PidifrkySettings.closestDistance)
              .andThenOnUIThread { case Success(merchs) =>
                addMarkers(merchs)
              }
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
        cameraMoved = true
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

    showItems()
  }

  override def onPrepareOptionsMenu(menu: Menu): Boolean = {
    import MenuKeys._

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
      showDefaultView()
      centerMapToCurrent()
      showItems()

    case _ =>
      Toast("Not supported", Toast.Long)
  }

  override protected def upButtonClicked(): Unit = {
    this.finish() //go back!
  }

  override def onMapLongClick(latLng: LatLng): Unit = {
    //TODO mocking on map
    LocationHandler.mockLocation(latLng)
  }
}

object MapActivity {

  object BundleKeys {
    private val prefix = getClass.getName + "_"

    final val CardsIds = prefix + "cardsIds"
    final val MerchantsIds = prefix + "merchsIds"
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
      case AllCards.id => AllCards
      case NearestCards.id => NearestCards
      case NearestMerchants.id => NearestMerchants
    }

    case object AllCards extends ViewType(0)

    case object NearestCards extends ViewType(1)

    case object NearestMerchants extends ViewType(2)

  }


}
