package cz.jenda.pidifrky.ui.api

import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.text.style.ImageSpan
import android.text.{SpannableStringBuilder, Spanned}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.{Application, DebugReporter}

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

  override def getPageTitle(position: Int): CharSequence = {
    val sb = new SpannableStringBuilder("                 "); // space added before text for convenience

    //    val drawable = Application.currentActivity.get.getResources.getDrawable(R.drawable.ic_gps_fixed_white_36dp)
    //
    //    drawable.setBounds(0, 0, 50, 50)

    Application.currentActivity.foreach { ctx =>
      val span = new ImageSpan(ctx, R.drawable.ic_gps_fixed_white_36dp)
      sb.setSpan(span, 5, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }


    sb
  }


}

trait TabFragment extends BasicFragment {
  val title: String

  def onShow(): Unit = {}

  def onHide(): Unit = {}
}