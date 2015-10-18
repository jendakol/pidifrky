package cz.jenda.pidifrky.logic

import com.afollestad.materialdialogs.MaterialDialog
import com.sromku.simple.storage.{AbstractDiskStorage, SimpleStorage}
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic.exceptions.NoStorageException
import cz.jenda.pidifrky.ui.api.BasicActivity
import cz.jenda.pidifrky.ui.dialogs.{DialogResultCallback, IndexDialogResult, SingleChoiceDialog}

import scala.concurrent.{Future, Promise}
import scala.util.Success

/**
 * @author Jenda Kolena, kolena@avast.com
 */
object StorageHandler {

  protected var storage: Option[AbstractDiskStorage] = None

  def init(implicit ctx: BasicActivity): Future[Unit] = {
    val p = Promise[Unit]()

    import com.sromku.simple.storage.SimpleStorage.StorageType._

    val external = SimpleStorage.getExternalStorage
    val internal = SimpleStorage.getInternalStorage(ctx)

    PidifrkySettings.storageType match {
      case Some(INTERNAL) =>
        storage = Some(internal)
        p.complete(Success(()))
      case Some(EXTERNAL) =>
        if (external.isWritable)
          storage = Some(external)
        else {
          //TODO - fail when ext is selected but not writable ?
          storage = Some(internal)
        }
        p.complete(Success(()))
      case None =>
        if (external.isWritable) {
          SingleChoiceDialog('selectstorage, R.string.storage_title_select, R.array.storageTypes, new DialogResultCallback[IndexDialogResult] {
            override def onDialogResult(dialogId: Symbol, dialog: MaterialDialog, result: IndexDialogResult): Unit = {
              result.index match {
                case 0 =>
                  storage = Some(external)
                  PidifrkySettings.setStorageType(EXTERNAL)
                case 1 =>
                  storage = Some(internal)
                  PidifrkySettings.setStorageType(INTERNAL)
              }

              p.complete(Success(()))
            }
          }).show()
        } else {
          //ext is not writable, sad...
          storage = Some(internal)
          PidifrkySettings.setStorageType(INTERNAL)
          p.complete(Success(()))
        }
    }

    p.future
  }

  def withStorage[T](f: AbstractDiskStorage => T) =
    storage.map(f).getOrElse({
      DebugReporter.debugAndReport(NoStorageException)
      null
    })
}
