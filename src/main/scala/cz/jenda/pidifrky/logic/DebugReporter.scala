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

  def debug(e: Throwable, msg: String = ""): Unit = debug(
    Format(e) + (if (StringUtils.isNotBlank(msg)) s"($msg)" else "")
  )

  def debug(msg: String): Unit = Log.d(DEBUG_TAG, msg)

  def debugAndReport(e: Throwable, msg: String = ""): Unit = {
    debug(e, msg)
    Mint.addExtraData("Ignored", "true")

    Application.currentActivity.foreach { ctx =>
      val description = new StandardExceptionParser(ctx, null).getDescription(Thread.currentThread.getName, e)
      debug(description)
      ctx.getTracker.foreach(_.send(new HitBuilders.ExceptionBuilder().setDescription(description).setFatal(false).build))
    }

    Mint.logExceptionMessage("Comment", msg, e match {
      case e: Exception => e
      case t: Throwable => new Exception(t)
    })
  }

  def breadcrumb(msg: String): Unit = {
    debug(msg)
    Mint.leaveBreadcrumb(msg)
  }
}
