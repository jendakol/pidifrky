package cz.jenda.pidifrky.ui

import android.os.Bundle
import android.view.View
import com.avast.android.dialogs.fragment.SimpleDialogFragment
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.ui.api.BasicActivity

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardsListActivity extends BasicActivity {
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    findTextView(R.id.textview).foreach(_.setText(R.string.hello_world))
  }

  def click(v: View): Unit = {
    SimpleDialogFragment.createBuilder(this, getSupportFragmentManager).setMessage("test string").setPositiveButtonText("OK").show()
  }
}
