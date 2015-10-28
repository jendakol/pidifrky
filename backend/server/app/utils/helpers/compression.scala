package utils.helpers

import java.io.{BufferedOutputStream, ByteArrayInputStream, ByteArrayOutputStream}
import java.nio.file.{Files, Path}
import java.util.zip.{Deflater, GZIPInputStream, GZIPOutputStream}

import com.google.common.io.ByteStreams
import logic.AppModule
import org.kamranzafar.jtar.{TarEntry, TarOutputStream}

import scala.concurrent.Future

/**
  * @author Jenda Kolena, kolena@avast.com
  */
object GZipHelper {
  def compress(data: Array[Byte]): Future[Array[Byte]] = Future {
    val os = new ByteArrayOutputStream
    val gos = new GZIPOutputStream(os) {
      `def`.setLevel(Deflater.BEST_COMPRESSION)
    }
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

object TarGzHelper {
  def compress(files: Seq[Path]): Future[Array[Byte]] = Future {
    val bos = new ByteArrayOutputStream()
    val gzos = new GZIPOutputStream(bos)
    val tar = new TarOutputStream(new BufferedOutputStream(gzos))

    files.foreach { file =>
      tar.putNextEntry(new TarEntry(file.toFile, file.getFileName.toString))
      tar.write(Files.readAllBytes(file))
    }

    tar.flush()
    tar.close()

    bos.toByteArray
  }(AppModule.blockingExecutor)
}