package cz.jenda.pidifrky.data

import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}
import cz.jenda.pidifrky._
import cz.jenda.pidifrky.data.dao.InsertCommand
import cz.jenda.pidifrky.logic.Application.executionContext
import cz.jenda.pidifrky.logic.{Application, DebugReporter, PidifrkyConstants, Transaction}

import scala.concurrent.Future
import scala.util.{Failure, Try}

/**
 * @author Jenda Kolena, kolena@avast.com
 */
object Database extends SQLiteOpenHelper(Application.appContext.orElse(Application.currentActivity).getOrElse({
  DebugReporter.debugAndReport(new Exception("Missing context for database connection"))
  null
}), PidifrkyConstants.DATABASE_NAME, null, PidifrkyConstants.DATABASE_VERSION) {

  protected lazy val db: SQLiteDatabase = getWritableDatabase

  override def onCreate(db: SQLiteDatabase): Unit = {
    try {
      DebugReporter.debug("Creating DB scheme")

      //merchants table
      db execSQL
        s"""CREATE TABLE IF NOT EXISTS ${MerchantsTable.NAME} (
         ${MerchantsTable.COL_ID} INTEGER PRIMARY KEY ASC,
         ${MerchantsTable.COL_NAME} TEXT,
         ${MerchantsTable.COL_NAME_RAW} TEXT,
         ${MerchantsTable.COL_ADDRESS} TEXT,
         ${MerchantsTable.COL_GPS_LAT} REAL,
         ${MerchantsTable.COL_GPS_LON} REAL,
         ${MerchantsTable.COL_GPS_PRECISE} INTEGER,
         ${MerchantsTable.COL_CARDS_IDS} TEXT
         )"""

      //cards table
      db execSQL
        s"""CREATE TABLE IF NOT EXISTS ${CardsTable.NAME} (
          ${CardsTable.COL_ID} INTEGER PRIMARY KEY ASC,
          ${CardsTable.COL_NUMBER} INTEGER UNIQUE,
          ${CardsTable.COL_NAME} TEXT,
          ${CardsTable.COL_NAME_RAW} TEXT,
          ${CardsTable.COL_IMAGE} TEXT,
          ${CardsTable.COL_GPS_LAT} REAL,
          ${CardsTable.COL_GPS_LON} REAL,
          ${CardsTable.COL_MERCHANTS_IDS} TEXT,
          ${CardsTable.COL_NEIGHBOURS} TEXT
        )"""

      //cards-status table; prepared for upcoming sync functionality
      db execSQL
        s"""CREATE TABLE IF NOT EXISTS ${CardStatusTable.NAME} (
         ${CardStatusTable.COL_CARD_ID} INTEGER,
         ${CardStatusTable.COL_TYPE} INTEGER,
         ${CardStatusTable.COL_ADDED} INTEGER,
         ${CardStatusTable.COL_REMOVED} INTEGER
              )"""

      //additional indexes
      db execSQL s"CREATE INDEX IF NOT EXISTS idx1 on ${CardStatusTable.NAME}(${CardStatusTable.COL_CARD_ID} ASC)"


      //new table has to be reflected also in getCursor method!!!

    }
    catch {
      case e: Exception =>
        DebugReporter.debugAndReport(e, "Error in creating the database")
        throw e; //otherwise it will be considered as done!
    }
  }

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int): Unit = {
    if (oldVersion < 10) {
      //old Pidifrky app!

      db execSQL "drop table " + CardsTable.NAME
      db execSQL "drop table " + MerchantsTable.NAME
      db execSQL "drop table " + CardStatusTable.NAME

      onCreate(db)
      //TODO - DB upgrade from old app!
    } else {
      //just normal upgrade
      //TODO upgrade
    }
  }

  def analyze(): Try[Unit] = Transaction("tables-analyze") {
    DebugReporter.debug("Running analyze of tables")
    db.execSQL("analyze " + CardsTable.NAME)
    db.execSQL("analyze " + MerchantsTable.NAME)
    db.execSQL("analyze " + CardStatusTable.NAME)
  }

  def truncate(table: EntityTable): Try[Unit] = Transaction("truncate-" + table.NAME) {
    db.execSQL("delete from " + table.NAME)
  }

  def rawQuery(query: String, selectionSeq: Map[String, AnyVal] = Map()): Future[CursorWrapper] = Future {
    DebugReporter.debug("DB query: " + query)

    val selection = if (selectionSeq.nonEmpty) {
      " where " + selectionSeq.keys map { key =>
        s"$key = ?"
      } mkString " and "
    } else ""

    val args = selectionSeq.values.map(_.toString).toArray

    new CursorWrapper(db.rawQuery(query + selection, args))
  }.andThen {
    case Failure(e) => DebugReporter.debugAndReport(e, "Error in raw query")
  }

  protected def selectFrom(table: EntityTable, columns: Array[String])(selectionSeq: Map[String, AnyVal], orderBy: Option[String], limit: Option[String]): Future[CursorWrapper] = Future {
    DebugReporter.debug(s"DB select from ${table.NAME}, columns ${columns.mkString("(", ", ", ")")}")

    val selection = selectionSeq.keys map { key =>
      s"$key = ?"
    } mkString " and "

    val args = selectionSeq.values.map(_.toString).toArray

    new CursorWrapper(db.query(table.NAME, columns, selection, args, null, null, orderBy.orNull, limit.orNull))
  }

  //missing error log in method above; is called only from method below

  def selectFrom(table: EntityTable)(selectionSeq: Map[String, AnyVal], orderBy: Option[String], limit: Option[String]): Future[CursorWrapper] = (table match {
    case MerchantsTable | CardsTable | CardStatusTable =>
      selectFrom(table, table.getColumns)(selectionSeq, orderBy, limit)
    case _ =>
      DebugReporter.debugAndReport(new IllegalArgumentException(s"Unsupported table '$table'"))
      Future.successful(new CursorWrapper(null))
  }).andThen {
    case Failure(e) => DebugReporter.debugAndReport(e, "Error in DB select query")
  }

  def executeTransactionally(commands: Seq[InsertCommand]): Future[Unit] = Transaction.async("db-transaction") {
    Future {
      DebugReporter.debug(s"Executing DB transaction with ${commands.length} commands")

      db.beginTransaction()
      commands foreach { command =>
        DebugReporter.debug(s"Executing on DB: ${command.query} (${command.args.mkString(", ")}})")
        db.execSQL(command.query, command.args)
      }
      db.setTransactionSuccessful()
      db.endTransaction()
    }(Application.executionContext).andThen {
      case Failure(e) => DebugReporter.debugAndReport(e, "Error in DB transaction")
    }
  }
}
