package cz.jenda.pidifrky.logic.http

import java.io.{ByteArrayInputStream, InputStream, UnsupportedEncodingException}

import cz.jenda.pidifrky.logic.DebugReporter

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
case class HttpResponse(status: Int, protected val bytes: Array[Byte], originalSize: Int) {

  def asInputStream: InputStream = new ByteArrayInputStream(bytes)

  def asString: String = try
    new String(bytes, "utf-8")
  catch {
    case e: UnsupportedEncodingException =>
      DebugReporter.debugAndReport(e)
      new String(bytes)
  }

}
