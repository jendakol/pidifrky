package cz.jenda.pidifrky.ui.fragments

import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.TextView
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.ui.api.TabFragment

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class TestTabFragment2 extends TabFragment {
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    super.onCreateView(inflater, container, savedInstanceState)

    val view = inflater.inflate(R.layout.main, container, false)

    Option(view.findViewById(R.id.textview).asInstanceOf[TextView]).foreach(_.setText("Jupiii"))

    view
  }

  override val title: String = "test Tab 2"
}
