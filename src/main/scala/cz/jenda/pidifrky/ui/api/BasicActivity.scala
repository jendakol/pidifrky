package cz.jenda.pidifrky.ui.api

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.{Menu, MenuItem}
import com.google.android.gms.analytics.{GoogleAnalytics, HitBuilders, Tracker}
import com.splunk.mint.Mint
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic._

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicActivity extends AppCompatActivity with ViewHandler with ActivityNavigator {

  protected val hasParentActivity = true

  private var activityState: ActivityState = _

  protected final implicit val ctx: AppCompatActivity = this

  private var appStart = false

  private var tracker: Option[Tracker] = None

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    DebugReporter.debug("Creating activity " + getLocalClassName)

    activityState = CreatedState

    appStart = Application.currentActivity.isEmpty

    PidifrkySettings.init(this)

    Mint.initAndStartSession(this, PidifrkyConstants.MINT_API_KEY)
    Mint.setUserIdentifier(PidifrkySettings.UUID)

    val actionBar = getSupportActionBar
    //    actionBar.setIcon(R.drawable.app_icon_bar)
    //    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME)
    actionBar.setHomeButtonEnabled(hasParentActivity)
    actionBar.setDisplayHomeAsUpEnabled(hasParentActivity)

    if (PidifrkySettings.isTrackingEnabled) {
      val analytics = GoogleAnalytics.getInstance(this)
      val tracker = analytics.newTracker(R.xml.app_tracker)

      tracker.setScreenName(getLocalClassName)
      tracker.setClientId(PidifrkySettings.UUID)
      tracker.setAppVersion(Utils.getAppVersionName(this))

      this.tracker = Some(tracker)
    }
  }

  override protected def onStart(): Unit = {
    super.onStart()
    activityState = StartedState
    DebugReporter.debug("Starting activity " + getLocalClassName)
    Application.currentActivity = Some(this)

    tracker.foreach(_.send(new HitBuilders.ScreenViewBuilder().build))
  }

  override protected def onPause(): Unit = {
    super.onPause()
    activityState = PausedState
    DebugReporter.debug("Pausing activity " + getLocalClassName)
  }


  override protected def onPostResume(): Unit = {
    super.onPostResume()
    DebugReporter.debug("Resuming activity " + getLocalClassName)

    try {
      if (appStart) onApplicationStart()
    }
    catch {
      case e: Exception => DebugReporter.debug(e)
    }
  }

  override protected def onStop(): Unit = {
    super.onStop()
    DebugReporter.debug("Stopping activity " + getLocalClassName)

    activityState = StoppedState
    Application.currentActivity = None
  }

  protected def actionBarMenu(): Option[Int] = None

  protected def onActionBarClicked: PartialFunction[Int, Boolean] = {
    case _ => false
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    actionBarMenu().foreach(menuId => getMenuInflater.inflate(menuId, menu))
    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = actionBarMenu().map { _ =>
    onActionBarClicked.apply(item.getItemId)
  }.getOrElse(super.onOptionsItemSelected(item))

  protected def onApplicationStart(): Unit = {}

  protected def onApplicationMinimize(): Unit = {}

  /* --- */

  def getTracker: Option[Tracker] = tracker
}