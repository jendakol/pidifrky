package cz.jenda.pidifrky.ui.api

import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Jenda Kolena, kolena@avast.com
 */
case class ElementId(value: Int)

object ElementId {
  private val i = new AtomicInteger(0)

  def apply(): ElementId = ElementId(i.incrementAndGet())
}
