package utils

import java.io.{InputStream, OutputStream}
import java.nio.file.{Files, Path, Paths}

import com.google.common.io.ByteStreams

import scala.util.Try

/**
  * @author Jenda Kolena, jendakolena@gmail.com
  */
case class StorageDir(rootDir: Path) {
  Files.createDirectories(rootDir)

  protected val path = rootDir.toAbsolutePath.toString

  def newFile(name: String): Path = Paths.get(path, name)

  def newFileStream(name: String): OutputStream = Files.newOutputStream(newFile(name))

  def saveNewFile(name: String, data: InputStream): Try[Path] = Try {
    val file = newFile(name)

    val os = Files.newOutputStream(file)
    ByteStreams.copy(data, os)
    os.close()

    file
  }

  def find(child: String): Option[Path] = {
    val file: Path = newFile(child)
    if (Files.exists(file)) Some(file) else None
  }

  def isWriteable: Boolean = Files.isWritable(rootDir)
}
