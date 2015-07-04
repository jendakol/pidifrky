package cz.jenda.pidifrky.ui.api

import android.content.Context
import android.support.v4.app.ListFragment
import cz.jenda.pidifrky.data.pojo.Entity
import cz.jenda.pidifrky.logic.listadapters.BasicAdapter

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait EntityListTabFragment[T <: Entity] extends ListFragment with TabFragment {
  protected val emptyText: String

  protected def listAdapter(ctx: Context): BasicAdapter[T]

  override def onStart(): Unit = {
    super.onStart()
    setListAdapter(listAdapter(getActivity))
    setEmptyText(emptyText)
  }
}
