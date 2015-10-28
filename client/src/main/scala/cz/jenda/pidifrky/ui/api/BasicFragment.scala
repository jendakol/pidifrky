package cz.jenda.pidifrky.ui.api

import android.support.v4.app.Fragment

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicFragment extends Fragment{
  protected final implicit val ctx: Fragment = this
}
