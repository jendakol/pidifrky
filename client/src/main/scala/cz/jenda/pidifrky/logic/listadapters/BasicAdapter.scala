package cz.jenda.pidifrky.logic.listadapters

import android.content.Context
import android.preference.PreferenceManager
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.BaseAdapter
import cz.jenda.pidifrky.data.pojo.Entity

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicAdapter[T <: Entity](ctx: Context, showLocation: Boolean) extends BaseAdapter {
  protected val inflater: LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
  protected val prefs = PreferenceManager.getDefaultSharedPreferences(ctx.getApplicationContext)

  protected var data: List[T]

  def updateData(list: List[T]): Unit = {
    this.data = list
  }

  override def getCount: Int = data.size

  override def getItemId(position: Int): Long = position

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val view = Option(convertView).getOrElse(inflater.inflate(getLayoutId, null))

    drawItem(getItem(position), view)

    view
  }

  protected def drawItem(item: T, view: View): Unit

  protected def getLayoutId: Int

  override def getItem(position: Int): T = data(position)
}
