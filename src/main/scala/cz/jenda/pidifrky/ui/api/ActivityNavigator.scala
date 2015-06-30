package cz.jenda.pidifrky.ui.api

import android.content.Intent
import android.support.v7.app.AppCompatActivity

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait ActivityNavigator extends AppCompatActivity {
  def goTo[A <: BasicActivity](newActivity: Class[A]): Unit = {
    startActivity(new Intent(this, newActivity))
  }
}
