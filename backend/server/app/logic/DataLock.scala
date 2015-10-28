package logic

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * @author Jenda Kolena, jendakolena@gmail.com
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

class Semaphore(capacity: Int) {
  private val s = new java.util.concurrent.Semaphore(capacity)

  def withLock[T](f: => T): T = try {
    s.acquire()
    f
  } finally s.release()

  def withLockAsync[T](f: => Future[T]): Future[T] = try {
    s.acquire()
    f.andThen {
      case _ => s.release()
    }
  } catch {
    case e: Exception =>
      s.release()
      Future.failed(e)
  }

  def lock(): Unit = s.acquire()

  def unlock(): Unit = s.release()
}

class Mutex extends Semaphore(1)