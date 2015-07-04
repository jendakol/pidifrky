package cz.jenda.pidifrky.logic.listadapters

import android.content.Context
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, TextView}
import cz.jenda.pidifrky.R

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicAdapter(ctx: Context) extends BaseAdapter {
  protected val inflater: LayoutInflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

  override def getCount: Int = 10

  override def getItemId(position: Int): Long = 1

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val view = Option(convertView).getOrElse(inflater.inflate(R.layout.main, null))

    view.findViewById(R.id.textview).asInstanceOf[TextView].setText("The item")

    view
  }

  override def getItem(position: Int): AnyRef = ???
}
