package cz.jenda.pidifrky.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import cz.jenda.pidifrky.R

/**
 * Created <b>28.6.2015</b><br>
 *
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class StartActivity extends AppCompatActivity {
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    findViewById(R.id.textview).asInstanceOf[TextView].setText(R.string.hello_world)
  }
}
