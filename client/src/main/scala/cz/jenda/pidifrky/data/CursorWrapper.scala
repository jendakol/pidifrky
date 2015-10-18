package cz.jenda.pidifrky.data

import java.io.Closeable

import android.database.Cursor
import cz.jenda.pidifrky.data.pojo.{Entity, EntityFactory}
import cz.jenda.pidifrky.logic.DebugReporter
import cz.jenda.pidifrky.logic.exceptions.EntityNotFoundException

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
 * @author Jenda Kolena, kolena@avast.com
 */
class CursorWrapper(cursor: Cursor) extends Closeable {
  DebugReporter.debug("Loaded cursor with size " + cursor.getCount)

  def mapTo[E <: Entity](ef: EntityFactory[E])(implicit ct: ClassTag[E]): Try[E] = if (nonEmpty) {
    cursor.moveToFirst()
    ef.create(cursor)
  } else Failure(EntityNotFoundException(ct.runtimeClass.getSimpleName))

  def mapToList[E <: Entity](ef: EntityFactory[E])(implicit ct: ClassTag[E]): Try[Seq[E]] = if (nonEmpty) Try {
    cursor.moveToFirst()

    val builder = Seq.newBuilder[Try[E]]

    while (!cursor.isAfterLast) {
      builder += ef.create(cursor)
      cursor.moveToNext()
    }

    builder.result().flatMap {
      case Success(e) => Some(e)
      case Failure(ex) =>
        DebugReporter.debugAndReport(ex, s"Cannot convert cursor to ${ct.runtimeClass.getSimpleName} entity")
        None
    }
  } else Failure(EntityNotFoundException(ct.runtimeClass.getSimpleName))

  def map[A](f: Cursor => A): Try[Seq[A]] = if (nonEmpty) Try {
    cursor.moveToFirst()

    val builder = Seq.newBuilder[Try[A]]

    while (!cursor.isAfterLast) {
      builder += Try(f(cursor))
      cursor.moveToNext()
    }

    builder.result().flatMap {
      case Success(e) => Some(e)
      case Failure(ex) =>
        DebugReporter.debugAndReport(ex, s"Cannot map cursor")
        None
    }
  } else Success(Seq())


  def isEmpty: Boolean = cursor == null || cursor.getCount <= 0

  def nonEmpty: Boolean = !isEmpty

  override def close(): Unit = cursor.close()
}
