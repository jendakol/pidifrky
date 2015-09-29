package cz.jenda.pidifrky.ui.lists

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.{LayoutInflater, View, ViewGroup}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.{Merchant, Card, Entity}

import scala.collection.SortedSet

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicListAdapter[E <: Entity](showLocation: Boolean)(implicit ctx: Context) extends RecyclerView.Adapter[AbstractViewHolder[E]] {
  protected var data: List[E]

  def updateData(data: SortedSet[E]): Unit = {
    this.data = data.toList
    notifyDataSetChanged()
  }

  override def getItemCount: Int =
    data.size

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

class CardsListAdapter(protected var data: List[Card], showLocation: Boolean)(implicit ctx: Context) extends BasicListAdapter[Card](showLocation) {

  def this(showLocation: Boolean)(implicit ctx: Context) = this(List(), showLocation)

  override protected val layoutId: Int = R.layout.cards_list_item

  override def createViewHolder(view: View): AbstractViewHolder[Card] = new CardViewHolder(view, showLocation)
}

class MerchantsListAdapter(protected var data: List[Merchant], showLocation: Boolean)(implicit ctx: Context) extends BasicListAdapter[Merchant](showLocation) {

  def this(showLocation: Boolean)(implicit ctx: Context) = this(List(), showLocation)

  override protected val layoutId: Int = R.layout.merchants_list_item

  override def createViewHolder(view: View): AbstractViewHolder[Merchant] = new MerchantViewHolder(view, showLocation)
}
