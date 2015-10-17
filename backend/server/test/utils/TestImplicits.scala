package utils

import scala.concurrent.{Await, Future}

/**
  * @author Jenda Kolena, kolena@avast.com
  */
object TestImplicits {

  implicit class WaitForFuture[T](val f: Future[T]) extends AnyVal {

    import scala.concurrent.duration._

    def block: T = Await.result(f, 5.seconds)
  }

}
