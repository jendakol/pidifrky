package utils.helpers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import com.google.common.io.ByteStreams
import logic.AppModule

import scala.concurrent.Future

/**
 * @author Jenda Kolena, kolena@avast.com
 */
object GZipHelper {
  def compress(data: Array[Byte]): Future[Array[Byte]] = Future {
    val os = new ByteArrayOutputStream
    val gos = new GZIPOutputStream(os)
    gos.write(data)
    gos.close()
    os.toByteArray
  }(AppModule.blockingExecutor)

  def decompress(data: Array[Byte]): Future[Array[Byte]] = Future {
    val is = new ByteArrayInputStream(data)
    val gis = new GZIPInputStream(is)

    ByteStreams.toByteArray(gis)
  }(AppModule.blockingExecutor)
}
