package cz.jenda.pidifrky.logic.map

import android.location.Location
import com.google.android.gms.maps.model.{BitmapDescriptorFactory, MarkerOptions}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.{Card, CardState, Merchant}

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
  override val drawableId: Int = card.state match {
    case CardState.NONE => R.drawable.card
    case CardState.OWNED => R.drawable.card_found
    case CardState.WANTED => R.drawable.card_wanted
  }

  override val location: Location = card.location.get

  override val title: String = card.name //TODO
}

case class MerchantMapMarker(merchant: Merchant) extends MapMarker {
  override val drawableId: Int = R.drawable.merchant
  override val location: Location = merchant.location.get
  override val title: String = merchant.name
}
