package cz.jenda.pidifrky.ui.api

import android.location.Location
import android.os.Bundle
import android.support.v4.app.{NavUtils, TaskStackBuilder}
import android.support.v7.app.AppCompatActivity
import android.view.{Menu, MenuItem}
import com.google.android.gms.analytics.{GoogleAnalytics, HitBuilders, Tracker}
import com.splunk.mint.Mint
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic._
import cz.jenda.pidifrky.logic.location.LocationHandler

import scala.concurrent.Future

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
abstract class BasicActivity extends AppCompatActivity with ViewHandler with ActivityNavigator {

  protected def hasParentActivity = true

  private var activityState: ActivityState = CreatedState //default

  protected final implicit val ctx: BasicActivity = this

  private var appStart = false

  private var rotating = false

  protected def actionBarMenu(): Option[Int] = None

  private var tracker: Option[Tracker] = None

  protected var mockLocation: Boolean = false //default

  protected implicit final val ec = Application.executionContext

  protected var initFuture: Future[Boolean] = _

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    Application.appContext = Some(getApplicationContext)

    initFuture = Application.init

    DebugReporter.debug("Creating activity " + getLocalClassName)

    activityState = CreatedState

    appStart = Application.currentActivity.isEmpty

    rotating = Application.currentActivity.exists(_.equals(BasicActivity.this)) && Application.currentOrientation != getOrientation

    Application.currentOrientation = getOrientation

    Mint.initAndStartSession(this, getString(R.string.MINT_API_KEY))
    Mint.setUserIdentifier(PidifrkySettings.UUID)

    mockLocation = Option(savedInstanceState).exists { bundle =>
      bundle.getBoolean("mockLocation")
    }

    if (!mockLocation) {
      LocationHandler.start
    }

    Toast.onRestoreState(savedInstanceState)

    val actionBar = getSupportActionBar
    actionBar.setHomeButtonEnabled(hasParentActivity)
    actionBar.setDisplayHomeAsUpEnabled(hasParentActivity)

    if (PidifrkySettings.trackingEnabled) {
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

    LocationHandler.addListener(onLocationChanged)

    tracker.foreach(_.send(new HitBuilders.ScreenViewBuilder().build))
  }

  override protected def onPause(): Unit = {
    super.onPause()
    activityState = PausedState
    DebugReporter.debug("Pausing activity " + getLocalClassName)

    LocationHandler.stop
    LocationHandler.removeListener(onLocationChanged)
  }

  override protected def onPostResume(): Unit = {
    super.onPostResume()
    DebugReporter.debug("Resuming activity " + getLocalClassName)

    initFuture.foreach { init =>
      try {
        if (init && appStart) {
          DebugReporter.debug("Running onApplicationStart")
          onApplicationStart()
        }
      }
      catch {
        case e: Exception => DebugReporter.debugAndReport(e, "Error while executing onApplicationStart")
      }
    }
  }

  override def onSaveInstanceState(outState: Bundle): Unit = {
    super.onSaveInstanceState(outState)
    Toast.onSaveState(outState)
  }

  override protected def onStop(): Unit = {
    super.onStop()
    DebugReporter.debug("Stopping activity " + getLocalClassName)

    if (Application.currentActivity.exists(_.equals(BasicActivity.this)) && Application.currentOrientation == getOrientation) {
      try {
        onApplicationMinimize()
      }
      catch {
        case e: Exception => DebugReporter.debugAndReport(e, "Error while minimizing the app")
      }
    }

    activityState = StoppedState
  }

  protected def onActionBarClicked: PartialFunction[Int, Boolean] = {
    case _ => false
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    actionBarMenu().foreach(menuId => getMenuInflater.inflate(menuId, menu))
    super.onCreateOptionsMenu(menu)
  }

  protected def upButtonClicked(): Unit = {
    val upIntent = NavUtils.getParentActivityIntent(this)

    if (upIntent != null) {
      if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
        TaskStackBuilder.create(this)
          .addNextIntentWithParentStack(upIntent)
          .startActivities()
      }
      else {
        NavUtils.navigateUpTo(this, upIntent)
      }
    }
    else {
      DebugReporter.breadcrumb("upIntent is null")
    }
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val id = item.getItemId
    if (id == android.R.id.home) {
      upButtonClicked()
      true
    }
    else {
      actionBarMenu()
        .map { _ => if (onActionBarClicked.isDefinedAt(id)) onActionBarClicked.apply(id) else false }
        .getOrElse(super.onOptionsItemSelected(item))
    }
  }

  protected def runOnUiThread(block: => Unit): Unit = runOnUiThread(new Runnable {
    override def run(): Unit = block
  })

  protected def runAsync(block: => Unit): Unit = ec.execute(new Runnable {
    override def run(): Unit = block
  })

  protected def onLocationChanged(location: Location): Unit = {}

  protected def onApplicationStart(): Unit = {
    DebugReporter.debug("Application has started")
  }

  protected def onApplicationMinimize(): Unit = {
    DebugReporter.debug("Application has been minimized")
  }

  /* --- */

  protected def isRotating: Boolean = rotating

  def getOrientation: Orientation = Orientation(getWindowManager.getDefaultDisplay.getRotation)

  def getTracker: Option[Tracker] = tracker
}