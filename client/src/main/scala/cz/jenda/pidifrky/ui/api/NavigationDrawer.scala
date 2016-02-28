package cz.jenda.pidifrky.ui.api

import android.content.res.Configuration
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.{ActionBarDrawerToggle, AppCompatActivity}
import android.view.{MenuItem, View}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.DebugReporter

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait NavigationDrawer extends AppCompatActivity with ViewHandler with NavigationView.OnNavigationItemSelectedListener {

  private var drawerLayout: Option[DrawerLayout] = None
  private var drawerToggle: Option[ActionBarDrawerToggle] = None

  private var actTitle = "Pidifrky" //default

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    findView(R.id.main_drawer, classOf[NavigationView]).foreach { drawer =>
      drawer.setNavigationItemSelectedListener(this)

      Option(getSupportActionBar).foreach { actionBar =>
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setHomeButtonEnabled(true)
      }

      drawerLayout = findView(R.id.drawer_layout, classOf[DrawerLayout])

      drawerToggle = drawerLayout.map { layout =>
        val toggle = new ActionBarDrawerToggle(NavigationDrawer.this, layout, R.string.drawer_open, R.string.drawer_close) {
          override def onDrawerClosed(drawerView: View): Unit = {
            super.onDrawerClosed(drawerView)

            Option(getSupportActionBar).foreach { actionBar =>
              actionBar.setTitle(actTitle)
              invalidateOptionsMenu()
            }
          }

          override def onDrawerOpened(drawerView: View): Unit = {
            super.onDrawerOpened(drawerView)

            actTitle = getTitle.toString

            Option(getSupportActionBar).foreach { actionBar =>
              actionBar.setTitle(R.string.app_name)
              invalidateOptionsMenu()
            }
          }
        }

        toggle.setDrawerIndicatorEnabled(true)
        layout.setDrawerListener(toggle)

        toggle
      }
    }
  }


  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    //is the drawer toggle able to handle the event?
    drawerToggle.exists(_.onOptionsItemSelected(item)) || super.onOptionsItemSelected(item)
  }

  override def onPostCreate(savedInstanceState: Bundle): Unit = {
    super.onPostCreate(savedInstanceState)

    drawerToggle.foreach(_.syncState())
  }

  override def onConfigurationChanged(newConfig: Configuration): Unit = {
    super.onConfigurationChanged(newConfig)

    drawerToggle.foreach(_.onConfigurationChanged(newConfig))
  }

  protected def onNavigationDrawerClick: PartialFunction[Int, Unit]

  override def onNavigationItemSelected(item: MenuItem): Boolean = {
    drawerLayout.foreach(_.closeDrawer(GravityCompat.START))

    val id = item.getItemId

    DebugReporter.debug(s"Clicked '${item.getTitle}' on navigation drawer")

    if (onNavigationDrawerClick.isDefinedAt(id)) {
      onNavigationDrawerClick.apply(id)
      true
    } else {
      false
    }
  }
}
