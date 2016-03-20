package cz.jenda.pidifrky.ui.fragments

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.view._
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.data.pojo.Entity
import cz.jenda.pidifrky.logic.{ActivityState, DebugReporter, Utils}
import cz.jenda.pidifrky.ui.MapActivity
import cz.jenda.pidifrky.ui.api._

import scala.util.control.NonFatal

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class MainFragment extends BasicFragment {

  protected val tabs = Seq[EntityListTabFragment[_ <: Entity]](CardsAllListFragment(), CardsNearestListFragment(), MerchantsNearestListFragment())

  private var pagerAdapter: FragmentPagerAdapter = _

  private var currentTab: Option[PagerTabFragment] = None

  protected lazy val preselectedTabIndex: Int = preselect

  //it's mutable because it's set in onCreate
  private var preselect = 1

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    setHasOptionsMenu(true)
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    inflater.inflate(R.layout.fragment_main, container, false)
  }

  override def onActivityCreated(savedInstanceState: Bundle): Unit = {
    super.onActivityCreated(savedInstanceState)
    withCurrentActivity { implicit ctx =>
      preselect = ctx.getIntent.getIntExtra(MapActivity.BundleKeys.ViewType, 1)

      pagerAdapter = new PidifrkyPagerAdapter(getChildFragmentManager, tabs)

      findView(R.id.pager, classOf[ViewPager]).foreach { pager =>
        pager.setAdapter(pagerAdapter)
        pager.addOnPageChangeListener(new OnPageChangeListener {
          private var selected = math.min(tabs.size, preselectedTabIndex)

          if (tabs.nonEmpty) {
            pager.setCurrentItem(selected)

            try {
              val preselectedTab = tabs(selected)
              preselectedTab.visible = true
              Utils.runOnUiThread(preselectedTab.onShow())

              currentTab = Option(preselectedTab)
              invalidateOptionsMenu()
            } catch {
              case NonFatal(e) => DebugReporter.debugAndReport(e)
            }
          }

          override def onPageScrollStateChanged(state: Int): Unit = {}

          override def onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int): Unit = {}

          override def onPageSelected(position: Int): Unit = {

            val newTab = tabs(position)
            newTab.visible = true
            newTab.getActivity match {
              case ba: BasicActivity =>
                ba.getState match {
                  case ActivityState.Started => Utils.runOnUiThread(newTab.onShow())
                  case _ => //wrong state of activity
                }
              case _ => //null or weird activity
            }

            try {
              val oldTab = tabs(selected)
              oldTab.visible = false
              Utils.runOnUiThread(oldTab.onHide())
            } catch {
              case NonFatal(e) => DebugReporter.debugAndReport(e)
            }

            DebugReporter.debug(s"Select tab index $position, class ${newTab.getClass.getSimpleName}, title ${newTab.title}")

            selected = position
            currentTab = Option(newTab)

            Utils.runOnUiThread {
              pager.invalidate()
              if (pager.beginFakeDrag()) {
                pager.fakeDragBy(0.1f)
                pager.endFakeDrag()
              }
              invalidateOptionsMenu()
            }
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
  }

  override protected def onResume(): Unit = {
    super.onResume()

    currentTab.foreach(_.onShow())
  }

  override protected def onStop(): Unit = {
    super.onStop()

    currentTab.foreach(_.onHide())
  }


  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater): Unit = {
    super.onCreateOptionsMenu(menu, inflater)

    currentTab.foreach { currentTab =>
      currentTab.actionBarMenuResourceId match {
        case Some(id) =>
          try {
            menu.clear()

            inflater.inflate(id, menu)
            currentTab.onMenuInflate(menu)
          }
          catch {
            case NonFatal(e) => DebugReporter.debug(e, "Error while inflating menu for tab")
          }
          true

        case None => false
      }
    }
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    try {
      currentTab.foreach { currentTab =>
        currentTab.onMenuAction.applyOrElse(item.getItemId, { _: Int =>
          DebugReporter.debug(s"Menu action is not defined for item '${item.getTitle}'")
          ()
        })
      }
    }
    catch {
      case NonFatal(e) => DebugReporter.debug(e, "Error while executing options menu callback")
    }

    super.onOptionsItemSelected(item)
  }
}
