package cz.jenda.pidifrky.ui.api

import android.view.Menu

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait PagerTabFragment extends BasicFragment {
  def title: Option[String]

  def iconResourceId: Option[Int]

  def actionBarMenuResourceId: Option[Int]

  def onShow(): Unit = {}

  def onHide(): Unit = {}

  def onMenuInflate(menu: Menu): Unit

  def onMenuAction: PartialFunction[Int, Unit]

  private[ui] var visible = false

  //the onShow is called here instead of in onPageSelected, because it's too early there (before attach to activity)
  override def onResume(): Unit = {
    super.onResume()

    if (visible) {
      onShow()
    }
  }
}
