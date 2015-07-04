package cz.jenda.pidifrky.logic.listadapters

import android.content.Context
import android.view.View
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.logic.PidifrkyConstants
import cz.jenda.pidifrky.ui.api.ViewHandler

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardsAdapter(ctx: Context, var data: List[Card], showLocation: Boolean) extends BasicAdapter[Card](ctx, showLocation) {

  override protected def getLayoutId: Int = R.layout.cards_list_item

  override protected def drawItem(card: Card, vi: View): Unit = {
    ViewHandler.findTextView(vi, R.id.name).foreach(_.setText((if (prefs.getBoolean(PidifrkyConstants.PREF_SHOW_CARDS_NUMBERS, false)) card.number + " - " else "") + card.name))

    ViewHandler.findTextView(vi, R.id.distance).foreach(distance => {
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
