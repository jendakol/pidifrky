package cz.jenda.pidifrky.logic

import java.io._
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.Executors

import android.content.{Context, SharedPreferences}
import android.util.Log
import com.google.android.gms.analytics.{HitBuilders, StandardExceptionParser}
import com.splunk.mint.Mint
import org.apache.commons.lang3.StringUtils

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
object DebugReporter {
  final val DEBUG_TAG = "PIDIFRKY"

  final val MAX_SIZE = 500000

  private var statsWriter: Option[StatsWriter] = None

  private lazy val formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSS")

  private lazy val executor = Executors.newScheduledThreadPool(1)

  private lazy val changeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
    def onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
      debug("Debug collecting: %b", PidifrkySettings.debugCollecting)
      debug("Debug autoSend: %b", PidifrkySettings.debugAutoSend)
    }
  }

  PidifrkySettings.sharedPreferences.foreach(_.registerOnSharedPreferenceChangeListener(changeListener))


  def debug(e: Throwable, msg: String = ""): Unit = debug(
    Format(e) + (if (StringUtils.isNotBlank(msg)) s"($msg)" else "")
  )

  def debug(msg: String, args: Any*): Unit = if (Utils.isDebug) Log.d(DEBUG_TAG, msg.format(args))

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

  def collectIfAllowed(msg: => String)(implicit ctx: Context): Unit = {
    if (!PidifrkySettings.debugCollecting) return

    executor.execute(new Runnable {
      override def run(): Unit = try {
        var stackTraceElement = new Exception().getStackTrace()(1)
        var i = 2
        while (stackTraceElement.getMethodName != null && stackTraceElement.getMethodName.startsWith("debug")) {
          stackTraceElement = new Exception().getStackTrace()({
            i += 1
            i - 1
          })
        }
        val text = stackTraceElement.getClassName + "." + stackTraceElement.getMethodName + ":" + stackTraceElement.getLineNumber + " - " + msg

        changeListener.synchronized {
          val writer = statsWriter.getOrElse {
            val file = new File(ctx.getFilesDir.getAbsolutePath + File.separator + "debugAndReport.log")
            val writer = StatsWriter(file, new PrintWriter(new BufferedWriter(new FileWriter(file, true))))
            statsWriter = Some(writer)
            writer
          }

          val date = formatter.format(new Date)

          writer.append(date).append(" ").append(text.replace("\n", "\\n")).append("\n").flush()
        }
      }
      catch {
        case e: Exception => Mint.logExceptionMessage("Cannot append to debugAndReport file", "ignored", e)
      }
    })
  }

  def sendIfAllowed(): Unit = {
    if (!PidifrkySettings.debugCollecting) return

    changeListener.synchronized {
      Application.currentActivity.foreach { implicit ctx =>
        statsWriter flatMap { wr =>
          val size = wr.file.length
          if (size == 0) return

          val builder = new StringBuilder
          var linesCount = 0

          try {
            val inputStreamReader = new InputStreamReader(new FileInputStream(wr.file))
            if (size > MAX_SIZE) {
              inputStreamReader.skip(size - MAX_SIZE) //keep last x MB
            }
            val reader = new BufferedReader(inputStreamReader)
            builder.append(Utils.getDeviceInfo)
            var line: String = null
            while ( {
                      line = reader.readLine
                      line
                    } != null) {
              builder.append(line).append("\n")
              linesCount += 1
            }
            debug("Debug lines collected")
          }
          catch {
            case e: IOException =>
              debugAndReport(e, "Cannot read file with debugAndReport")
              return
          }


          ???

          //TODO
        }
      }
    }
  }
}

case class StatsWriter(file: File, writer: PrintWriter) {
  def append(t: String): StatsWriter = {
    writer.append(t)
    this
  }

  def flush(): Unit = writer.flush()
}
