package cz.jenda.pidifrky.ui.fragments

import cz.jenda.pidifrky.data.pojo.Card
import cz.jenda.pidifrky.ui.api.{BasicActivity, EntityListTabFragment}
import cz.jenda.pidifrky.ui.lists.{BasicListAdapter, CardsListAdapter}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class CardsListFragment(implicit ctx: BasicActivity) extends EntityListTabFragment[Card] {
  override val title: String = "test Tab"

  override protected def listAdapter: BasicListAdapter[Card] = {
    val card: Card = Card(1, 1, "Main", "main", None, "", "")
    val data = List(card, card, card, card, card, card, card, card, card, card, card, card, card, card, card)
    new CardsListAdapter(ctx, data, false)
  }
}
