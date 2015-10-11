package logic

import java.util.concurrent.Semaphore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * @author Jenda Kolena, kolena@avast.com
 */
object DataLock {
  private val mutex = new Mutex
  private val lock = new Mutex
  @volatile
  private var readers = 0

  def withWriteLock[T](f: => Future[T]): Future[T] =
    lock.withLock(f)

  def withReadLock[T](f: => Future[T]): Future[T] = {
    mutex.withLock {
      readers += 1
      if (readers == 1) lock.lock()
    }

    f andThen {
      case _ =>
        mutex.withLock {
          readers -= 1
          if (readers == 0) lock.unlock()
        }
    }
  }
}

class Mutex {
  private val s = new Semaphore(1)

  def withLock[T](f: => T): T = try {
    s.acquire()
    f
  } finally s.release()

  def lock(): Unit = s.acquire()

  def unlock(): Unit = s.release()
}