package cz.jenda.pidifrky.ui

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.DebugReporter
import cz.jenda.pidifrky.logic.http.HttpRequester
import cz.jenda.pidifrky.logic.location.GpsLogger
import cz.jenda.pidifrky.ui.api.BasicActivity

class GpsLogActivity extends BasicActivity {
  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_gps_log)

    for {
      bundle <- Option(savedInstanceState)
      textView <- findTextView(R.id.gpslog)
    } yield {
      textView.setText(bundle.getString("gpslog", ""))
    }

    findTextView(R.id.gpslog).foreach(_.setMovementMethod(new ScrollingMovementMethod()))

    GpsLogger.setListener(event => findTextView(R.id.gpslog).foreach { textView =>
      val text = textView.getText
      textView.setText(text + event + "\n")
    })
  }


  override protected def onStart(): Unit = {
    super.onStart()

  }

  override def onSaveInstanceState(outState: Bundle): Unit = {
    super.onSaveInstanceState(outState)
    findTextView(R.id.gpslog).foreach(t => outState.putString("gpslog", t.getText.toString))
  }
}