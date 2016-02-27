package cz.jenda.pidifrky.data

import java.text.Collator
import java.util.Locale

import android.location.Location
import cz.jenda.pidifrky.data.pojo.{Card, Merchant}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object CardOrdering {

  object Implicits {
    implicit final val ByName = CardOrdering.ByName
  }

  case object ByName extends Ordering[Card] {
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

  case class ByDistance(currentLocation: Location) extends Ordering[Card] {

    override def compare(x: Card, y: Card): Int = {
      val opt = for {
        first <- Option(x)
        second <- Option(y)
        firstDistance <- first.getDistance(currentLocation)
        secondDistance <- second.getDistance(currentLocation)
      } yield {
          firstDistance.compareTo(secondDistance)
        }

      opt.getOrElse(ByName.compare(x, y))
    }
  }

}

object MerchantOrdering {

  object Implicits {
    implicit final val ByName = MerchantOrdering.ByName
  }

  case object ByName extends Ordering[Merchant] {
    private val c = Collator.getInstance(new Locale("cs", "CZ"))

    override def compare(x: Merchant, y: Merchant): Int = {
      val opt = for {
        first <- Option(x)
        second <- Option(y)
      } yield {
          c.compare(first.name, second.name)
        }

      opt.getOrElse(0)
    }
  }

  case class ByDistance(currentLocation: Location) extends Ordering[Merchant] {

    override def compare(x: Merchant, y: Merchant): Int = {
      val opt = for {
        first <- Option(x)
        second <- Option(y)
        firstDistance <- first.getDistance(currentLocation)
        secondDistance <- second.getDistance(currentLocation)
      } yield {
          firstDistance.compareTo(secondDistance)
        }

      opt.getOrElse(ByName.compare(x, y))
    }
  }

}

