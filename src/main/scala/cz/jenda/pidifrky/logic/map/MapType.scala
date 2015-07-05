package cz.jenda.pidifrky.logic.map

import com.google.android.gms.maps.GoogleMap

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
sealed trait MapType {
  val id: Int
}

object MapType {
  def apply(id: Int) = id match {
    case GoogleMap.MAP_TYPE_NORMAL => NormalMapType
    case GoogleMap.MAP_TYPE_SATELLITE => SatteliteMapType
    case GoogleMap.MAP_TYPE_HYBRID => HybridMapType
  }
}

case object NormalMapType extends MapType {
  override val id: Int = GoogleMap.MAP_TYPE_NORMAL
}

case object SatteliteMapType extends MapType {
  override val id: Int = GoogleMap.MAP_TYPE_SATELLITE
}

case object HybridMapType extends MapType {
  override val id: Int = GoogleMap.MAP_TYPE_HYBRID
}
