package cz.jenda.pidifrky.logic

import java.io._
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import org.kamranzafar.jtar.{TarEntry, TarInputStream, TarOutputStream}

import scala.concurrent.Future

/**
 * @author Jenda Kolena, kolena@avast.com
 */
object TarGzHelper {

  import Application.executionContext

  def compress(files: Seq[File]): Future[Array[Byte]] = Future {
    val bos = new ByteArrayOutputStream()
    val gzos = new GZIPOutputStream(bos)
    val tar = new TarOutputStream(new BufferedOutputStream(gzos))
    try {

      val buffer = new Array[Byte](2048)

      files.foreach { file =>
        tar.putNextEntry(new TarEntry(file, file.getName))
        writeToTar(new BufferedInputStream(new FileInputStream(file)))
      }

      def writeToTar(input: InputStream): Unit = {
        var closed = false
        while (!closed) {
          input.read(buffer) match {
            case len if len > -1 =>
              tar.write(buffer, 0, len)
            case _ =>
              input.close()
              closed = true
          }
        }
      }

      tar.flush()
    }
    finally {
      tar.close()
      gzos.close()
    }

    bos.toByteArray
  }

  def decompress(is: InputStream)(f: String => File): Future[Unit] = Future {
    val tar = new TarInputStream(new BufferedInputStream(new GZIPInputStream(is)))

    try {
      var done = false

      val buffer = new Array[Byte](2048)

      while (!done) {
        tar.getNextEntry match {
          case e: TarEntry =>
            val dest = f(e.getName)
            readFromTar(new BufferedOutputStream(new FileOutputStream(dest)))
          case _ => done = true
        }
      }

      def readFromTar(output: OutputStream): Unit = {
        var closed = false
        while (!closed) {
          tar.read(buffer) match {
            case len if len > -1 =>
              output.write(buffer, 0, len)
            case _ =>
              output.close()
              closed = true
          }
        }
      }
    }
    finally tar.close()
  }
}

