package cz.jenda.pidifrky.ui.lists

import java.util.Locale

import android.support.v7.widget.RecyclerView
import android.view.View
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.{Card, Entity, Merchant}
import cz.jenda.pidifrky.logic.location.LocationHandler
import cz.jenda.pidifrky.logic.{Application, PidifrkySettings}
import cz.jenda.pidifrky.ui.api.{BasicActivity, ViewHandler}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class AbstractViewHolder[E <: Entity](view: View) extends RecyclerView.ViewHolder(view) {
  protected implicit final val ec = Application.executionContext

  //TODO add showLocation
  def updateWith(entity: E)(implicit ctx: BasicActivity): Unit
}

class CardViewHolder(view: View, showLocation: Boolean) extends AbstractViewHolder[Card](view) {

  override def updateWith(card: Card)(implicit ctx: BasicActivity): Unit = {
    ViewHandler.findTextView(view, R.id.name).foreach { nameField =>
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

    ViewHandler.findImageView(view, R.id.thumb).foreach { view =>
      card.getThumbImageUri.foreach(view.setImageURI)
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

  override def updateWith(merchant: Merchant)(implicit ctx: BasicActivity): Unit = {
    ViewHandler.findTextView(view, R.id.name).foreach(_.setText(merchant.name))
    ViewHandler.findTextView(view, R.id.address).foreach(_.setText(merchant.address))

    for {
      loc <- merchant.location
      currentLoc <- LocationHandler.getCurrentLocation
      distanceView <- ViewHandler.findTextView(view, R.id.distance)
    } yield {
      val dist = currentLoc.distanceTo(loc) / 1000.0

      distanceView.setText("%.1f km".formatLocal(Locale.US, dist))
    }
  }
}
