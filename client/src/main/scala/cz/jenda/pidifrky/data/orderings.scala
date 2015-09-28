package cz.jenda.pidifrky.data

import java.text.Collator
import java.util.Locale

import cz.jenda.pidifrky.data.pojo.Card

/**
 * @author Jenda Kolena, kolena@avast.com
 */
object CardOrdering {

  object ByName extends Ordering[Card] {
    private val c = Collator.getInstance(new Locale("cs", "CZ"))

    override def compare(x: Card, y: Card): Int = {
      val opt = for {
        first <- Option(x)
        second <- Option(y)
      } yield {
          c.compare(first.name, second.name)
        }

      opt.getOrElse(0)
    }
  }

}

