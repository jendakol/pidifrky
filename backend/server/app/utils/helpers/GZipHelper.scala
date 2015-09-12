package utils.helpers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import com.google.common.io.ByteStreams

import scala.util.Try

/**
 * @author Jenda Kolena, kolena@avast.com
 */
object GZipHelper {
  def compress(data: Array[Byte]): Try[Array[Byte]] = Try {
    val os = new ByteArrayOutputStream
    val gos = new GZIPOutputStream(os)
    gos.write(data)
    gos.close()
    os.toByteArray
  }

  def decompress(data: Array[Byte]): Try[Array[Byte]] = Try {
    val is = new ByteArrayInputStream(data)
    val gis = new GZIPInputStream(is)

    ByteStreams.toByteArray(gis)
  }
}
