package cz.jenda.pidifrky.ui.api

import android.support.v4.app.Fragment

/**
 * @author Jenda Kolena, kolena@avast.com
 */
abstract class BasicFragment extends Fragment{
  protected final implicit val ctx: Fragment = this
}
