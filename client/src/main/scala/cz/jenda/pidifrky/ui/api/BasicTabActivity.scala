package cz.jenda.pidifrky.ui.api

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.DebugReporter

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicTabActivity extends BasicActivity {

  protected def tabs: List[TabFragment]

  protected def preselectedTabIndex: Int

  private var pagerAdapter: FragmentPagerAdapter = _

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

          try tabs(preselectedTabIndex).onShow() catch {
            case e: Exception => DebugReporter.debugAndReport(e)
          }
        }

        override def onPageScrollStateChanged(state: Int): Unit = {}

        override def onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int): Unit = {}

        override def onPageSelected(position: Int): Unit = {
          try tabs(position).onShow() catch {
            case e: Exception => DebugReporter.debugAndReport(e)
          }
          try tabs(selected).onHide() catch {
            case e: Exception => DebugReporter.debugAndReport(e)
          }

          selected = position
        }
      })

      findView(R.id.tabs, classOf[TabLayout]).foreach { tabLayout =>
        tabLayout.setupWithViewPager(pager)

        for (i <- tabs.indices) {
          tabs(i).icon.foreach { iconRes =>
            tabLayout.getTabAt(i).setIcon(iconRes)
          }
        }
      }
    }
  }
}

class PidifrkyPagerAdapter(fragmentManager: FragmentManager, tabs: List[TabFragment]) extends FragmentPagerAdapter(fragmentManager) {
  override def getItem(position: Int): Fragment = tabs(position)

  override def getCount: Int = tabs.size

  override def getPageTitle(position: Int): CharSequence = tabs(position).title.orNull

}

trait TabFragment extends BasicFragment {
  val title: Option[String]

  val icon: Option[Int]

  def onShow(): Unit = {}

  def onHide(): Unit = {}
}