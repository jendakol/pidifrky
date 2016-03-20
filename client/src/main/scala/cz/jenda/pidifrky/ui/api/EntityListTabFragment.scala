package cz.jenda.pidifrky.ui.api

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.{LayoutInflater, View, ViewGroup}
import com.malinskiy.superrecyclerview.SuperRecyclerView
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.Entity
import cz.jenda.pidifrky.logic.{Application, DebugReporter}
import cz.jenda.pidifrky.ui.MainActivity
import cz.jenda.pidifrky.ui.lists.BasicListAdapter

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class EntityListTabFragment[T <: Entity] extends BasicFragment with PagerTabFragment {

  protected def preload = true

  protected def listAdapter: BasicListAdapter[T]

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    inflater.inflate(R.layout.recyclerview, container, false)
  }

  override def onActivityCreated(savedInstanceState: Bundle): Unit = {
    super.onActivityCreated(savedInstanceState)

    findView(R.id.list, classOf[SuperRecyclerView]).foreach { recyclerView =>
      try {
        val adapter: BasicListAdapter[T] = listAdapter

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity))
        recyclerView.setAdapter(adapter)

        Application.withCurrentContext { implicit ctx =>
          RecyclerViewItemClickListener(recyclerView, new OnItemClickListener {
            override def onClick(view: View, position: Int): Unit = {
              adapter.getItem(position).foreach { item =>
                DebugReporter.debug(s"Clicked on $item")

                EntityListTabFragment.this.onClick(item)

                withCurrentActivity {
                  case a: MainActivity => a.onEntityClick(item)
                  case _ => DebugReporter.debug("Could not invoke callback, attached to wrong activity")
                }
              }
            }

            override def onLongClick(view: View, position: Int): Unit = {
              adapter.getItem(position).foreach { item =>
                DebugReporter.debug(s"long-clicked on $item")

                EntityListTabFragment.this.onLongClick(item)

                withCurrentActivity {
                  case a: MainActivity => a.onEntityLongClick(item)
                  case _ => DebugReporter.debug("Could not invoke callback, attached to wrong activity")
                }
              }
            }
          })
        }
      }
      catch {
        case e: Exception =>
          DebugReporter.debugAndReport(e, "Could not set list adapter")
      }
    }
  }

  def onClick(entity: T): Unit

  def onLongClick(entity: T): Unit
}
