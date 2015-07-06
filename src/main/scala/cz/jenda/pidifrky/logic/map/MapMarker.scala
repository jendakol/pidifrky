package cz.jenda.pidifrky.logic.map

import android.location.Location
import com.google.android.gms.maps.model.{BitmapDescriptorFactory, MarkerOptions}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.data.{CardNormalState, CardOwnedState, CardWantedState}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
sealed trait MapMarker {
  val drawableId: Int
  val title: String
  val location: Location

  def getMarker: MarkerOptions = new MarkerOptions()
    .title(title)
    .icon(BitmapDescriptorFactory.fromResource(drawableId))
    .position(LocationHelper.toLatLng(location))
}

case class CardMapMarker(card: Card) extends MapMarker {
  override val drawableId: Int = card.getState match {
    case CardNormalState => R.drawable.card
    case CardOwnedState => R.drawable.card_found
    case CardWantedState => R.drawable.card_wanted
  }
  override val location: Location = card.gps.get
  override val title: String = card.name //TODO
}