package cz.jenda.pidifrky.ui.lists

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.{LayoutInflater, View, ViewGroup}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.{Card, Entity}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicListAdapter[E <: Entity](ctx: Context, showLocation: Boolean) extends RecyclerView.Adapter[AbstractViewHolder[E]] {
  protected var data: List[E]

  def updateData(data: Set[E]): Unit = {
    this.data = data.toList
    notifyDataSetChanged()
  }

  override def getItemCount: Int = data.size

  override def onBindViewHolder(holder: AbstractViewHolder[E], position: Int): Unit = {
    holder.updateWith(data(position))
  }

  override def onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder[E] = {
    val view = LayoutInflater.from(parent.getContext).inflate(layoutId, parent, false)

    createViewHolder(view)
  }

  override def getItemId(position: Int): Long = position

  protected def layoutId: Int

  def createViewHolder(view: View): AbstractViewHolder[E]

}

class CardsListAdapter(ctx: Context, var data: List[Card], showLocation: Boolean) extends BasicListAdapter[Card](ctx, showLocation) {

  override protected val layoutId: Int = R.layout.cards_list_item

  override def createViewHolder(view: View): AbstractViewHolder[Card] = new CardViewHolder(view, showLocation)
}

//TODO - merchant list adapter
