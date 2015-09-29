package cz.jenda.pidifrky.ui.lists

import android.support.v7.widget.RecyclerView
import android.view.View
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.{Card, Entity, Merchant}
import cz.jenda.pidifrky.logic.PidifrkySettings
import cz.jenda.pidifrky.ui.api.ViewHandler

/**
 * @author Jenda Kolena, kolena@avast.com
 */
abstract class AbstractViewHolder[E <: Entity](view: View) extends RecyclerView.ViewHolder(view) {

  def updateWith(entity: E): Unit
}

class CardViewHolder(view: View, showLocation: Boolean) extends AbstractViewHolder[Card](view) {
  override def updateWith(card: Card): Unit = {
    ViewHandler.findTextView(view, R.id.name).foreach(_.setText((if (PidifrkySettings.showCardsNumbers) card.number + " - " else "") + card.name))

    ViewHandler.findTextView(view, R.id.distance).foreach(distance => {
      if (showLocation) {
        card.getDistance.foreach { d =>
          val dist = d / 1000d
          distance.setText(if (dist > 0) "%.2f km".format(dist) else "")
        }
      }
      else {
        distance.setText("")
      }
    })

    //TODO - card view


    //    val thumb_image: ImageView = vi.findViewById(R.id.list_image).asInstanceOf[ImageView]
    //    val smiley_image: ImageView = vi.findViewById(R.id.list_state).asInstanceOf[ImageView]


    //    thumb_image.setImageURI(Utils.getThumbUri(card))

    //    if (card.isOwner) {
    //      smiley_image.setImageResource(R.drawable.smiley)
    //      vi.findViewById(R.id.corner).setVisibility(View.VISIBLE)
    //    }
    //    else {
    //      if (card.isWanted) {
    //        smiley_image.setImageResource(R.drawable.checklist)
    //        vi.findViewById(R.id.corner).setVisibility(View.VISIBLE)
    //      }
    //      else {
    //        smiley_image.setImageBitmap(null)
    //        vi.findViewById(R.id.corner).setVisibility(View.GONE)
    //      }
    //    }
  }
}

class MerchantViewHolder(view: View, showLocation: Boolean) extends AbstractViewHolder[Merchant](view) {
  override def updateWith(merchant: Merchant): Unit = {
    ViewHandler.findTextView(view, R.id.name).foreach(_.setText(merchant.name))
    //TODO - merchant view
  }
}
