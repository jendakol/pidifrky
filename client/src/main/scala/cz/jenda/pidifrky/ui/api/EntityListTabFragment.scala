package cz.jenda.pidifrky.ui.api

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.{LayoutInflater, View, ViewGroup}
import com.malinskiy.superrecyclerview.SuperRecyclerView
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.Entity
import cz.jenda.pidifrky.logic.{Application, DebugReporter}
import cz.jenda.pidifrky.ui.lists.BasicListAdapter

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class EntityListTabFragment[T <: Entity] extends BasicFragment with TabFragment {

  protected def preload = true

  protected def listAdapter: BasicListAdapter[T]

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    val view = inflater.inflate(R.layout.recyclerview, container, false)

    ViewHandler.findView(view, R.id.list, classOf[SuperRecyclerView]).foreach { recyclerView =>
      try {
        val adapter: BasicListAdapter[T] = listAdapter

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity))
        recyclerView.setAdapter(adapter)

        Application.withCurrentContext { implicit ctx =>
          RecyclerViewItemClickListener(recyclerView, new OnItemClickListener {
            override def onClick(view: View, position: Int): Unit = {
              adapter.getItem(position).foreach(EntityListTabFragment.this.onClick)
            }

            override def onLongClick(view: View, position: Int): Unit = {
              adapter.getItem(position).foreach(EntityListTabFragment.this.onLongClick)
            }
          })
        }
      }
      catch {
        case e: Exception =>
          DebugReporter.debugAndReport(e, "Could not set list adapter")
      }
    }

    view
  }

  def onClick(entity: T)

  def onLongClick(entity: T)
}
