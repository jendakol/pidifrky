package cz.jenda.pidifrky.ui.fragments

import android.support.v4.app.ListFragment
import cz.jenda.pidifrky.logic.listadapters.BasicAdapter
import cz.jenda.pidifrky.ui.api.TabFragment

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class TestTabFragment extends ListFragment with TabFragment {


  override def onStart(): Unit = {
    super.onStart()
    setListAdapter(new BasicAdapter(this.getActivity) {})
    setEmptyText("EMPTY!!!")
    setListShown(true)
  }

  override def onResume(): Unit = {
    super.onResume()
  }

  override val title: String = "test Tab"
}
