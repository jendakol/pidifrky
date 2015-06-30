package cz.jenda.pidifrky.ui

import android.os.Bundle
import android.view.View
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.ui.api.BasicActivity

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class StartActivity extends BasicActivity {
  override protected val hasParentActivity = false

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    findTextView(R.id.textview).foreach(_.setText(R.string.hello_world))
  }

  def click(v: View): Unit = {
    goTo(classOf[CardsListActivity])
  }
}
