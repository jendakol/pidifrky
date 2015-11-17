package cz.jenda.pidifrky.logic.map

import android.location.Location
import com.google.android.gms.maps.LocationSource.OnLocationChangedListener
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.logic.location.LocationHandler.LocationListener
import cz.jenda.pidifrky.ui.api.ElementId

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object MapLocationSource extends com.google.android.gms.maps.LocationSource with LocationListener {
  protected final implicit val ElemId: ElementId = ElementId()

  private var listener: Option[OnLocationChangedListener] = None

  override def activate(onLocationChangedListener: OnLocationChangedListener): Unit = {
    listener = Some(onLocationChangedListener)
    LocationHandler.addListener(this)
  }

  override def deactivate(): Unit = {
    LocationHandler.removeListener
  }

  override def apply(location: Location): Unit = listener.foreach(_.onLocationChanged(location))
}
