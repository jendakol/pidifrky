package cz.jenda.pidifrky.ui.api

import android.support.v4.app.{Fragment, FragmentPagerAdapter, FragmentManager}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class PidifrkyPagerAdapter(fragmentManager: FragmentManager, tabs: Seq[PagerTabFragment]) extends FragmentPagerAdapter(fragmentManager) {
  override def getItem(position: Int): Fragment = tabs(position)

  override lazy val getCount: Int = tabs.size

  override def getPageTitle(position: Int): CharSequence = tabs(position).title.orNull

}
