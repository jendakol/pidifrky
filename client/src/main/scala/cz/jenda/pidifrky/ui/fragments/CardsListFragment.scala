package cz.jenda.pidifrky.ui.fragments

import android.content.Context
import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.logic.listadapters.{BasicAdapter, CardsAdapter}
import cz.jenda.pidifrky.ui.api.EntityListTabFragment

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardsListFragment extends EntityListTabFragment[Card] {
  override val title: String = "test Tab"
  override protected val emptyText: String = "empty list!!!"

  override protected def listAdapter(ctx: Context): BasicAdapter[Card] = {
    val card: Card = Card(1, 1, "Main", "main", None, "", "")
    val data = List(card, card, card, card, card, card, card, card, card, card, card, card, card, card, card)
    new CardsAdapter(ctx, data, false)
  }
}
