package cz.jenda.pidifrky.ui.api

import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentManager, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import cz.jenda.pidifrky.R

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

    val actionBar = getActionBar


    findView(R.id.pager, classOf[ViewPager]).foreach { pager =>
      pager.setAdapter(pagerAdapter)

    }
  }
}

class PidifrkyPagerAdapter(fragmentManager: FragmentManager, tabs: List[TabFragment]) extends FragmentPagerAdapter(fragmentManager) {
  override def getItem(position: Int): Fragment = tabs(position)

  override def getCount: Int = tabs.size

  override def getPageTitle(position: Int): CharSequence = tabs(position).title
}

trait TabFragment extends Fragment {
  val title: String
}