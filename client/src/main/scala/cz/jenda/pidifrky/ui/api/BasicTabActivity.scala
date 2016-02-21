package cz.jenda.pidifrky.ui.api

import android.content.Context
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.view.{Menu, MenuItem}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.DebugReporter

import scala.util.control.NonFatal

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicTabActivity extends BasicActivity {

  protected def tabs: Seq[TabFragment]

  protected def preselectedTabIndex: Int

  private var pagerAdapter: FragmentPagerAdapter = _

  private var currentTab: TabFragment = _

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.tabview)

    pagerAdapter = new PidifrkyPagerAdapter(getSupportFragmentManager, tabs)

    findView(R.id.pager, classOf[ViewPager]).foreach { pager =>
      pager.setAdapter(pagerAdapter)
      pager.addOnPageChangeListener(new OnPageChangeListener {
        private var selected = math.min(tabs.size, preselectedTabIndex)

        if (tabs.nonEmpty) {
          pager.setCurrentItem(selected)

          try {
            val preselectedTab = tabs(selected)
            preselectedTab.onShow()

            currentTab = preselectedTab
            invalidateOptionsMenu()
          } catch {
            case NonFatal(e) => DebugReporter.debugAndReport(e)
          }
        }

        override def onPageScrollStateChanged(state: Int): Unit = {}

        override def onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int): Unit = {}

        override def onPageSelected(position: Int): Unit = {

          val newTab = tabs(position)

          try {
            tabs(selected).onHide()
          } catch {
            case NonFatal(e) => DebugReporter.debugAndReport(e)
          }

          selected = position
          currentTab = newTab

          invalidateOptionsMenu()
        }
      })

      findView(R.id.tabs, classOf[TabLayout]).foreach { tabLayout =>
        tabLayout.setupWithViewPager(pager)

        for (i <- tabs.indices) {
          tabs(i).iconResourceId.foreach { iconRes =>
            tabLayout.getTabAt(i).setIcon(iconRes)
          }
        }
      }
    }
  }

  override def onPrepareOptionsMenu(menu: Menu): Boolean = Option(currentTab).exists { currentTab =>
    currentTab.actionBarMenuResourceId match {
      case Some(id) =>
        try {
          menu.clear()
          getMenuInflater.inflate(id, menu)
          currentTab.onMenuInflate(menu)
        }
        catch {
          case NonFatal(e) => DebugReporter.debug(e, "Error while inflating menu for tab")
        }
        true

      case None => false
    }
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    try {
      require(currentTab != null)

      currentTab.onMenuAction.applyOrElse(item.getItemId, { _: Int =>
        DebugReporter.debug(s"Menu action is not defined for item '${item.getTitle}'")
        ()
      })
    }
    catch {
      case NonFatal(e) => DebugReporter.debug(e, "Error while executing options menu callback")
    }

    super.onOptionsItemSelected(item)
  }
}

class PidifrkyPagerAdapter(fragmentManager: FragmentManager, tabs: Seq[TabFragment]) extends FragmentPagerAdapter(fragmentManager) {
  override def getItem(position: Int): Fragment = tabs(position)

  override def getCount: Int = tabs.size

  override def getPageTitle(position: Int): CharSequence = tabs(position).title.orNull

}

trait TabFragment extends BasicFragment {
  def title: Option[String]

  def iconResourceId: Option[Int]

  def actionBarMenuResourceId: Option[Int]

  def onShow(): Unit = {}

  def onHide(): Unit = {}

  def onMenuInflate(menu: Menu): Unit

  def onMenuAction: PartialFunction[Int, Unit]

  override def onResume(): Unit = {
    super.onResume()

    onShow()
  }
}