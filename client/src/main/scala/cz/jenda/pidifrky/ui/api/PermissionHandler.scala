package cz.jenda.pidifrky.ui.api

import java.util.concurrent.atomic.AtomicInteger

import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.DebugReporter
import cz.jenda.pidifrky.logic.exceptions.{PermissionNotGrantedException, PidifrkyException}
import cz.jenda.pidifrky.ui.api.PidifrkyPermissions.Permission
import cz.jenda.pidifrky.ui.dialogs.InfoDialog

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
trait PermissionHandler extends AppCompatActivity {
  this: BasicActivity =>

  import PackageManager._

  private var waitingPermissions: Map[Int, Promise[Unit]] = Map()

  def requestPermission(permission: Permission): Future[Unit] = LoadToast.withLoadToast(R.string.permission_requesting) {
    val notGranted = permission.native.map { permCode =>
      (permCode, ContextCompat.checkSelfPermission(this, permCode) == PERMISSION_GRANTED)
    }.flatMap { case (perm, res) => if (res) None else Some(perm) }

    if (notGranted.isEmpty) {
      DebugReporter.debug(s"Requesting already granted permission $permission")
      return Future.successful(())
    }

    DebugReporter.debug(s"Requesting permission $permission, ${notGranted.mkString("[", ", ", "]")} not granted yet")

    val explanation = {
      val explanationNeeded = notGranted.exists(ActivityCompat.shouldShowRequestPermissionRationale(this, _))

      val f = if (explanationNeeded) {
        DebugReporter.debug(s"Showing explanation for permission $permission")
        val dialog = InfoDialog('permissionExplanation, permission.explanationTitleId, permission.explanationTextId, cancellable = false)(this)
        dialog.show()
        dialog.future
      } else {
        DebugReporter.debug(s"No need to show explanation for permission $permission")
        Future.successful(())
      }

      f.map(_ => notGranted)
    }

    val request = explanation.flatMap { perms =>
      val f = waitingPermissions.getOrElse(permission.reqCode, {
        val p = Promise[Unit]()
        waitingPermissions = waitingPermissions + (permission.reqCode -> p)
        p
      }).future

      DebugReporter.debug(s"Calling native requestPermissions for ${perms.mkString(",")}")

      ActivityCompat.requestPermissions(this, perms.toArray, permission.reqCode)

      f
    }

    request
  }(this)

  override def onRequestPermissionsResult(requestCode: Int, permissions: Array[String], grantResults: Array[Int]): Unit = {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    waitingPermissions.get(requestCode) match {
      case Some(promise) =>
        waitingPermissions = waitingPermissions - requestCode
        if (grantResults.forall(_ == PERMISSION_GRANTED)) {
          DebugReporter.debug(s"All requested permissions (${permissions.mkString(",")}) were granted")
          promise.complete(Success(()))
        } else {
          DebugReporter.debug(s"Not all requested permissions  from ${permissions.mkString(", ")} were granted")
          promise.complete(Failure(PermissionNotGrantedException(permissions)))
        }

      case None =>
        DebugReporter.breadcrumb(s"Promise of permission granting not found (permissions: ${permissions.mkString(", ")})")
    }
  }
}

object PidifrkyPermissions {

  import android.Manifest.permission._

  sealed abstract class Permission(val reqCode: Int, val explanationTitleId: Int, val explanationTextId: Int, val native: String*)

  object Permission {
    def apply(native: String): Permission = native match {
      case WRITE_EXTERNAL_STORAGE => ExternalStorage
      case ACCESS_COARSE_LOCATION | ACCESS_FINE_LOCATION => Location

      case _ => throw new PidifrkyException(s"Unknown permission '$native'") {}
    }
  }


  private val id = new AtomicInteger(0)

  case object ExternalStorage extends Permission(id.incrementAndGet(), R.string.permission_storage_title, R.string.permission_storage, WRITE_EXTERNAL_STORAGE)

  case object Location extends Permission(id.incrementAndGet(), R.string.permission_location_title, R.string.permission_location, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)

}
