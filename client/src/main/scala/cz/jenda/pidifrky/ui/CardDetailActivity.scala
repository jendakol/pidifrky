package cz.jenda.pidifrky.ui

import android.os.Bundle
import android.view.View
import android.widget.{ImageView, TextView}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.dao.CardsDao
import cz.jenda.pidifrky.logic.FutureImplicits._
import cz.jenda.pidifrky.ui.api.{BasicActivity, Toast}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardDetailActivity extends BasicActivity {

  import CardDetailActivity._

  override protected val actionBarMenu = Some(R.menu.card_detail)

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.card_detail)

    val intent = getIntent

    val cardId = intent.getIntExtra(BundleKeys.CardId, 0)

    CardsDao.get(cardId).flatMapOnUIThread { card =>
      findView(R.id.name, classOf[TextView]).foreach(_.setText(card.name))
      findView(R.id.number, classOf[TextView]).foreach(_.setText(getText(R.string.number) + " " + card.number))

      card.getFullImageUri.mapOnUIThread { uri =>
        findView(R.id.image, classOf[ImageView]).foreach(_.setImageURI(uri))
      }
    }
  }

  def showBigImage(v: View): Unit = {
    Toast("Not supported", Toast.Short)
  }
}

object CardDetailActivity {

  object BundleKeys {
    private val prefix = getClass.getName + "_"

    final val CardId = prefix + "cardId"

  }

}
