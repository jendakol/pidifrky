package cz.jenda.pidifrky.ui.lists

import android.support.v7.widget.RecyclerView
import android.view.View
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.{Card, Entity, Merchant}
import cz.jenda.pidifrky.logic.PidifrkySettings
import cz.jenda.pidifrky.ui.api.ViewHandler

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class AbstractViewHolder[E <: Entity](view: View) extends RecyclerView.ViewHolder(view) {

  //TODO add showLocation
  def updateWith(entity: E): Unit
}

class CardViewHolder(view: View, showLocation: Boolean) extends AbstractViewHolder[Card](view) {
  override def updateWith(card: Card): Unit = {
    ViewHandler.findTextView(view, R.id.name).foreach{nameField=>
      val name = (if (PidifrkySettings.showCardsNumbers) card.number + " - " else "") + card.name
      nameField.setText(name)
    }

    ViewHandler.findTextView(view, R.id.distance).foreach(distanceField => {
      if (showLocation) {
        card.getDistance.foreach { d =>
          val dist = d / 1000d
          distanceField.setText(if (dist > 0) "%.2f km".format(dist) else "")
        }
      }
      else {
        distanceField.setText("")
      }
    })

    for {
      view <- ViewHandler.findImageView(view, R.id.thumb)
      thumbUri <- card.getThumbImageUri
    } yield {
      view.setImageURI(thumbUri)
    }

    ViewHandler.findImageView(view, R.id.status).foreach { statusView =>
      import cz.jenda.pidifrky.data.pojo.CardState._
      card.state match {
        case NONE =>
          //TODO statusView.setVisibility(View.GONE)
        case WANTED =>
          statusView.setVisibility(View.VISIBLE)
          statusView.setImageResource(R.drawable.checklist)
        case OWNED =>
          statusView.setVisibility(View.VISIBLE)
          statusView.setImageResource(R.drawable.smiley)
      }

    }
  }
}

class MerchantViewHolder(view: View, showLocation: Boolean) extends AbstractViewHolder[Merchant](view) {
  override def updateWith(merchant: Merchant): Unit = {
    ViewHandler.findTextView(view, R.id.name).foreach(_.setText(merchant.name))
    //TODO - merchant view
  }
}
