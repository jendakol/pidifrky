package cz.jenda.pidifrky.logic

import java.io._
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.Executors

import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.analytics.{HitBuilders, StandardExceptionParser}
import com.google.protobuf.{ByteString, TextFormat}
import com.splunk.mint.Mint
import cz.jenda.pidifrky.logic.http.HttpRequester
import cz.jenda.pidifrky.proto.DeviceBackend.DebugReportRequest
import org.apache.commons.lang3.StringUtils

import scala.util.{Failure, Success}

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

  def debug(msg: => String, args: Any*): Unit = {
    lazy val s = msg.format(args)
    if (Utils.isDebug) {
      Log.d(DEBUG_TAG, s)
    }
    collectIfAllowed(s)
  }

  def debugAndReport(e: Throwable, msg: String = ""): Unit = {
    debug(e, msg)
    Mint.addExtraData("Ignored", "true")

    Application.currentActivity.foreach { ctx =>
      val description = new StandardExceptionParser(ctx, null).getDescription(Thread.currentThread.getName, e)
      debug(description)
      collectIfAllowed(description)
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

  def collectIfAllowed(msg: => String): Unit = {
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
          Application.currentActivity.foreach { implicit ctx =>
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
      }
      catch {
        case e: Exception => Mint.logExceptionMessage("Cannot append to debugAndReport file", "ignored", e)
      }
    })
  }

  def sendIfAllowed(): Unit = {
    if (!PidifrkySettings.debugCollecting) return

    executor.submit(new Runnable {
      override def run(): Unit = {
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
                builder.append(TextFormat.printToString(Utils.getDeviceInfo))
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
                case e: Exception =>
                  debugAndReport(e, "Cannot process debug report file")
                  return
              }

              if (linesCount > 0) {
                val b = DebugReportRequest.newBuilder()
                PidifrkySettings.contact.foreach(b.setContact)
                b.setContent(ByteString.copyFrom(Utils.gzip(builder.toString().getBytes).getOrElse("Cannot GZIP data!".getBytes)))

                HttpRequester.debugReport(b.build()) match {
                  case Success(_) => DebugReporter.debug("Debug report successfully sent")
                  case Failure(e) => DebugReporter.debugAndReport(e, "Cannot send debug report")
                }

                None
              } else
                Some(wr) //no change
            }
          }
        }
      }
    })
  }
}

case class StatsWriter(file: File, writer: PrintWriter) {
  def append(t: String): StatsWriter = {
    writer.append(t)
    this
  }

  def flush(): Unit = writer.flush()
}
