package cz.jenda.pidifrky.logic.http

import java.util.zip.GZIPInputStream

import com.google.common.io.ByteStreams
import retrofit.client.Response
import retrofit.mime.TypedByteArray

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
sealed trait ResponseDecoder {
  def decode(r: Response): HttpResponse
}

case object PlainResponseDecoder extends ResponseDecoder {
  override def decode(r: Response): HttpResponse = {
    val body = Option(r.getBody)

    val content = body match {
      case Some(b: TypedByteArray) => b.getBytes
      case Some(f) =>
        ByteStreams.toByteArray(f.in())
      case None => Array[Byte]()
    }

    HttpResponse(r.getStatus, content, body.map(_.length().toInt).getOrElse(0))
  }
}

case object GZIPResponseDecoder extends ResponseDecoder {
  override def decode(r: Response): HttpResponse = {
    val body = Option(r.getBody)

    //missing error handling - it's handled in HttpRequester
    val content = body.map(b => ByteStreams.toByteArray(new GZIPInputStream(b.in()))).getOrElse(Array[Byte]())

    HttpResponse(r.getStatus, content, body.map(_.length().toInt).getOrElse(0))
  }
}
