package cz.jenda.pidifrky.ui.api

import android.os.Bundle
import com.google.android.gms.maps.{OnMapReadyCallback, SupportMapFragment}
import cz.jenda.pidifrky.R

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicMapActivity extends BasicActivity with OnMapReadyCallback {
  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.map)
    Option(getSupportFragmentManager.findFragmentById(R.id.map)) match {
      case Some(f: SupportMapFragment) => f.getMapAsync(this)
      case Some(f) => //TODO: warning
      case None => //TODO: warning
    }


  }


}
