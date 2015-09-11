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
    val body = r.getBody

    val content = body match {
      case b: TypedByteArray => b.getBytes
      case f =>
        ByteStreams.toByteArray(f.in())
    }

    HttpResponse(r.getStatus, content, body.length().toInt)
  }
}

case object GZIPResponseDecoder extends ResponseDecoder {
  override def decode(r: Response): HttpResponse = {
    val body = r.getBody

    //missing error handling - it's handled in HttpRequester
    val content = ByteStreams.toByteArray(new GZIPInputStream(body.in()))

    HttpResponse(r.getStatus, content, body.length().toInt)
  }
}
