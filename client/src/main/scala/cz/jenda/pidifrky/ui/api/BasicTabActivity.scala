package cz.jenda.pidifrky.ui.api

import android.os.Bundle
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

  private var pagerAdapter: FragmentPagerAdapter = _

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.pagerview)

    pagerAdapter = new PidifrkyPagerAdapter(getSupportFragmentManager, tabs)

    findView(R.id.pager, classOf[ViewPager]).foreach { pager =>
      pager.setAdapter(pagerAdapter)
      pager.addOnPageChangeListener(new OnPageChangeListener {
        private var selected = 0

        if (tabs.nonEmpty)
          try tabs.head.onShow() catch {
            case e: Exception => DebugReporter.debugAndReport(e)
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
    }

  }
}

class PidifrkyPagerAdapter(fragmentManager: FragmentManager, tabs: List[TabFragment]) extends FragmentPagerAdapter(fragmentManager) {
  override def getItem(position: Int): Fragment = tabs(position)

  override def getCount: Int = tabs.size

  override def getPageTitle(position: Int): CharSequence = tabs(position).title
}

trait TabFragment extends BasicFragment {
  val title: String

  def onShow(): Unit = {}

  def onHide(): Unit = {}
}