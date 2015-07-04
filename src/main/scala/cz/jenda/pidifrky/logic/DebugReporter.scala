package cz.jenda.pidifrky.logic

import android.util.Log
import com.google.android.gms.analytics.{HitBuilders, StandardExceptionParser}
import com.splunk.mint.Mint
import org.apache.commons.lang3.StringUtils

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object DebugReporter {
  final val DEBUG_TAG = "PIDIFRKY"

  def debug(e: Exception, msg: String = ""): Unit = Log.d(DEBUG_TAG, if (StringUtils.isNotBlank(msg)) msg else "")

  def debug(msg: String): Unit = Log.d(DEBUG_TAG, msg)

  def debugAndReport(e: Exception, msg: String = ""): Unit = {
    debug(e, msg)
    Mint.addExtraData("ignored", "true")

    Application.currentActivity.foreach { ctx =>
      val description = new StandardExceptionParser(ctx, null).getDescription(Thread.currentThread.getName, e)
      debug(description)
      ctx.getTracker.foreach(_.send(new HitBuilders.ExceptionBuilder().setDescription(description).setFatal(false).build))
    }

    Mint.logExceptionMessage("Comment", msg, e)
  }
}
