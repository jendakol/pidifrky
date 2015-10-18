package cz.jenda.pidifrky.logic

import java.io._
import java.net.{InetAddress, UnknownHostException}
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit}
import java.util.zip.GZIPOutputStream

import android.app.{Activity, AlarmManager, PendingIntent}
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.{Context, Intent}
import android.location.{Location, LocationManager}
import android.net.Uri
import android.os.{Build, Debug}
import android.view.Display
import com.splunk.mint.Mint
import cz.jenda.pidifrky.proto.DeviceBackend.Envelope.DeviceInfo

import scala.concurrent.Future
import scala.util.Try

/**
 * Created <b>3.4.13</b><br>
 *
 * @author Jenda Kolena, jendakolena@gmail.com
 * @version 0.1
 * @since 0.2
 */
object Utils {

  def copy(src: File, dst: File): Try[Unit] = Try {
    val in: InputStream = new FileInputStream(src)
    val out: OutputStream = new FileOutputStream(dst)
    val buf: Array[Byte] = new Array[Byte](1024)
    var len: Int = 0
    while ( {
      len = in.read(buf)
      len
    } > 0) {
      out.write(buf, 0, len)
    }
    in.close()
    out.close()
  }


  //  def getThumbUri(number: Int): Uri = {
  //    if (context == null || number <= 0) return null
  //    return getThumbUri(CardsDao.getInstance(context).getByNumber(number).asInstanceOf[Nothing])
  //  }
  //
  //  def getFullImageUri(number: Int): Uri = {
  //    if (context == null || number <= 0) return null
  //    return getFullImageUri(CardsDao.getInstance(context).getByNumber(number).asInstanceOf[Nothing])
  //  }

  def getThumbImageUri(cardId: Int): Option[Uri] = Application.currentActivity.map { context =>
    val file = new File(context.getExternalFilesDir(null) + File.separator + PidifrkyConstants.PATH_IMAGES_THUMBS + File.separator + cardId + ".jpg")
    if (!file.exists()) return None

    Uri.fromFile(file)
  }

  def getFullImageUri(cardId: Int): Option[Uri] = Application.currentActivity.map { context =>
    val file = new File(context.getExternalFilesDir(null) + File.separator + PidifrkyConstants.PATH_IMAGES_FULL + File.separator + cardId + ".jpg")
    if (!file.exists()) return None

    Uri.fromFile(file)
  }

  case class ScreenSize(width: Int, height: Int) {
    def toGpb: cz.jenda.pidifrky.proto.DeviceBackend.Envelope.DeviceInfo.ScreenSize =
      cz.jenda.pidifrky.proto.DeviceBackend.Envelope.DeviceInfo.ScreenSize.newBuilder()
        .setWidth(width)
        .setHeight(height)
        .build()
  }

  //noinspection ScalaDeprecation
  def getScreenSize(activity: Activity): ScreenSize = {
    val display: Display = activity.getWindowManager.getDefaultDisplay

    ScreenSize(display.getWidth, display.getHeight)
  }

  //  def noLocalizationException(context: Nothing, finalize: Boolean): Nothing = {
  //    if (context == null) return null
  //    DebugReporter.debug("No localization exception")
  //    return AlertDialogHandler.createWithButtons(context, R.string.app_name, R.string.error_no_localization_services, new Nothing(context.getString(R.string.button_settings), new DialogInterface.OnClickListener() {
  //      def onClick(dialog: DialogInterface, which: Int) {
  //        if (finalize) {
  //          context.finish
  //        }
  //        val intent: Intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
  //        dialog.dismiss
  //        context.startActivity(intent)
  //      }
  //    }), new Nothing(context.getString(R.string.button_continue), new DialogInterface.OnClickListener() {
  //      def onClick(dialog: DialogInterface, which: Int) {
  //        dialog.dismiss
  //        GpsHandler.getInstance(context).setDisabled(true)
  //        val intent: Intent = context.getIntent
  //        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
  //        context.startActivity(intent)
  //      }
  //    })).show
  //  }
  //
  //  def noInternetConnection(context: Nothing, finalize: Boolean): Nothing = {
  //    if (context == null) return null
  //    DebugReporter.debug("No internet connection exception")
  //    return AlertDialogHandler.createSimple(context, R.string.app_name, R.string.error_no_internet_connection, R.string.ok, new DialogInterface.OnClickListener() {
  //      def onClick(dialog: DialogInterface, which: Int) {
  //        if (finalize) {
  //          context.finish
  //        }
  //        val intent: Intent = new Intent(Settings.ACTION_SETTINGS)
  //        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
  //        context.startActivity(intent)
  //      }
  //    }).show
  //  }


  private val executorService: ScheduledExecutorService = Executors.newScheduledThreadPool(2)
  private var online: Boolean = true

  def updateOnlineStatus(): Future[Boolean] = Future {
    online = try {
      InetAddress.getAllByName("maps.google.com")
      true
    }
    catch {
      case e: UnknownHostException => false
      case e: Exception =>
        DebugReporter.debugAndReport(e)
        false
    }
    DebugReporter.debug("Online status: " + online)
    try {
      InetAddress.getAllByName("pidifrky.jenda.eu")
      true
    }
    catch {
      case e: Exception => DebugReporter.debugAndReport(e, "Cannot connect to service point")
        false
    }
  }(Application.executionContext)

  def isOnline: Boolean = online

  private var debug: Option[Boolean] = None

  def isDebug: Boolean = debug.getOrElse {
    val debug = Debug.isDebuggerConnected
    this.debug = Some(debug)
    DebugReporter.debug("Is debug: " + debug)
    if (debug) Mint.enableDebug()
    debug
  }

  def runOnUiThread(action: => Unit)(implicit ctx: Activity) {
    ctx.runOnUiThread(new Runnable {
      override def run(): Unit = action
    })
  }

  //  def dpToPx(pixels: Int): Int = {
  //    val displayMetrics: DisplayMetrics = getContext.getResources.getDisplayMetrics
  //    return pixels * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT).round
  //  }
  //
  //  def getDimension(resourceId: Int): Int = {
  //    return getContext.getResources.getDimension(resourceId).asInstanceOf[Int]
  //  }
  //
  //  def isAppInstalled(uri: String): Boolean = {
  //    val pm: PackageManager = context.getPackageManager
  //    var app_installed: Boolean = false
  //    try {
  //      pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
  //      app_installed = true
  //    }
  //    catch {
  //      case e: PackageManager.NameNotFoundException => {
  //        app_installed = false
  //      }
  //    }
  //    return app_installed
  //  }

  def restartApp(intent: Intent)(implicit ctx: Activity): Unit = {
    DebugReporter.debug("Restarting application")

    try {
      val mPendingIntent: PendingIntent = PendingIntent.getActivity(ctx, 123456, intent, PendingIntent.FLAG_CANCEL_CURRENT)
      val mgr: AlarmManager = ctx.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
      mgr.set(AlarmManager.RTC, System.currentTimeMillis + 100, mPendingIntent)
    }
    catch {
      case e: Exception => DebugReporter.debugAndReport(e)
    }

    System.exit(0)
  }

  def getAppVersionName(context: Activity): String = {
    try {
      context.getPackageManager.getPackageInfo(context.getPackageName, 0).versionName
    }
    catch {
      case e: PackageManager.NameNotFoundException =>
        DebugReporter.debug(e)
        "not found"
    }
  }

  //
  //  def toStringArray(selectionArgs: AnyRef*): Array[String] = {
  //    val args = new Array[String](if (selectionArgs != null) selectionArgs.length else 0) {
  //      var i: Int = 0
  //      while (i < args.) {
  //        args(i) = if (selectionArgs != null) if (selectionArgs(i) != null) selectionArgs(i).toString else "" else null
  //        {
  //          i += 1;
  //          i - 1
  //        }
  //      }
  //    }
  //    return args
  //  }

  def convertStreamToString(is: InputStream): String = {
    try {
      val reader: BufferedReader = new BufferedReader(new InputStreamReader(is))
      val sb: StringBuilder = new StringBuilder
      var line: String = null
      while ( {
        line = reader.readLine
        line
      } != null) {
        sb.append(line).append("\n")
      }
      reader.close()
      sb.toString()
    }
    catch {
      case e: IOException =>
        DebugReporter.debugAndReport(e)
        ""
    }
  }

  def getStringFromFile(fl: File): String = {
    try {
      val fin: FileInputStream = new FileInputStream(fl)
      val ret: String = convertStreamToString(fin)
      fin.close()
      ret
    }
    catch {
      case e: IOException =>
        DebugReporter.debugAndReport(e)
        ""
    }
  }

  //
  //  def openBrowserIntent(url: String): Intent = {
  //    DebugReporter.debug("Opening browser (%s)", url)
  //    val i: Intent = new Intent(Intent.ACTION_VIEW)
  //    i.setData(Uri.parse(url))
  //    return i
  //  }
  //
  //  def openReport(context: Activity, `type`: String, metadata: String, header: String, defaultText: String): Intent = {
  //    DebugReporter.debug("Opening report (%s,%s,%s,%s)", `type`, metadata, header, defaultText)
  //    val intent: Intent = new Intent(context, classOf[Nothing])
  //    intent.putExtra("type", `type`)
  //    intent.putExtra("metadata", metadata)
  //    intent.putExtra("header", header)
  //    if (defaultText != null) {
  //      intent.putExtra("defaultText", defaultText)
  //    }
  //    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
  //    return intent
  //  }
  //
  //  def openReport(context: Activity, `type`: String, metadata: String, header: String): Intent = {
  //    return openReport(context, `type`, metadata, header, null)
  //  }
  //
  //  def prepareForSearch(text: String): String = {
  //    return Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase
  //  }
  //

  def toLocation(lat: Double, lon: Double): Location = {
    val l = new Location(LocationManager.GPS_PROVIDER)
    l.setLatitude(lat)
    l.setLongitude(lon)
    l
  }

  def getAppVersion: String =
    Application.currentActivity.map { ctx =>
      try {
        val packageInfo = ctx.getPackageManager.getPackageInfo(ctx.getPackageName, 0)
        packageInfo.versionName + "/" + packageInfo.versionCode
      }
      catch {
        case e: NameNotFoundException =>
          //ignore...
          DebugReporter.debugAndReport(e)
          "unknown"
      }
    }.getOrElse("unknown")

  def getDeviceInfo(implicit activity: Activity): DeviceInfo = {
    DeviceInfo.newBuilder()
      .setDevice(Build.DEVICE)
      .setId(Build.ID)
      .setManufacturer(Build.MANUFACTURER)
      .setModel(Build.MODEL)
      .setHardware(Build.HARDWARE)
      .setScreenSize(Utils.getScreenSize(activity).toGpb)
      .setSdk(Build.VERSION.SDK_INT)
      .build()
  }

  def gzip(data: Array[Byte]): Try[Array[Byte]] = Try {
    val os = new ByteArrayOutputStream
    val gos = new GZIPOutputStream(os)
    gos.write(data)
    gos.close()
    os.toByteArray
  }

  executorService.scheduleAtFixedRate(new Runnable() {
    def run() {
      updateOnlineStatus()
    }
  }, 0, 1, TimeUnit.MINUTES)
}
