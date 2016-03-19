package cz.jenda.pidifrky.ui.lists

import android.support.v7.widget.RecyclerView
import android.view.{LayoutInflater, View, ViewGroup}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.{Card, Entity, Merchant}
import cz.jenda.pidifrky.logic.DebugReporter
import cz.jenda.pidifrky.ui.api.BasicActivity

import scala.collection.SortedSet

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicListAdapter[E <: Entity](showLocation: Boolean)(implicit ctx: BasicActivity) extends RecyclerView.Adapter[AbstractViewHolder[E]] {
  protected var data: Seq[E]

  def updateData(data: SortedSet[E]): Unit = {
    this.data = data.toList
    DebugReporter.debug("Updating data, new size " + data.size)
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

  def getItem(position: Int): Option[E] = {
    if (position < data.size && position >= 0) Option(data(position)) else None
  }

  def currentData: Seq[E] = data

  override def getItemId(position: Int): Long = position

  protected def layoutId: Int

  def createViewHolder(view: View): AbstractViewHolder[E]

}

class CardsListAdapter(protected var data: Seq[Card], showLocation: Boolean)(implicit ctx: BasicActivity) extends BasicListAdapter[Card](showLocation) {

  def this(showLocation: Boolean)(implicit ctx: BasicActivity) = this(Seq(), showLocation)

  override protected val layoutId: Int = R.layout.cards_list_item

  override def createViewHolder(view: View): AbstractViewHolder[Card] = new CardViewHolder(view, showLocation)
}

class MerchantsListAdapter(protected var data: Seq[Merchant], showLocation: Boolean)(implicit ctx: BasicActivity) extends BasicListAdapter[Merchant](showLocation) {

  def this(showLocation: Boolean)(implicit ctx: BasicActivity) = this(Seq(), showLocation)

  override protected val layoutId: Int = R.layout.merchants_list_item

  override def createViewHolder(view: View): AbstractViewHolder[Merchant] = new MerchantViewHolder(view, showLocation)
}
