package cz.jenda.pidifrky.logic.http

import java.io.{IOException, InputStream}
import java.lang.reflect.{InvocationTargetException, Type}

import com.google.protobuf.{AbstractMessageLite, MessageLite}
import cz.jenda.pidifrky.logic.{Application, PidifrkySettings, Utils}
import cz.jenda.pidifrky.proto.DeviceBackend.Envelope
import retrofit.converter.{ConversionException, Converter}
import retrofit.mime.{TypedByteArray, TypedInput, TypedOutput}

/**
 * @author Jenda Kolena, kolena@avast.com
 */
/* This class is based on retrofit.converter.ProtoConverter */
object DeviceEnvelopeConverter extends Converter {
  private val MIME_TYPE: String = "application/x-protobuf"

  override def toBody(`object`: scala.Any): TypedOutput = `object` match {
    case gpb: MessageLite =>
      val data = gpb.toByteString

      implicit val ctx = Application.currentActivity.getOrElse(throw new IllegalStateException("No context is available"))

      val cont = Envelope.newBuilder()
        .setUuid(PidifrkySettings.UUID)
        .setDebug(Utils.isDebug)
        .setAppVersion(Utils.getAppVersion)
        .setDeviceInfo(Utils.getDeviceInfo)
        .setData(data)
        .build()
        .toByteArray

      new TypedByteArray(MIME_TYPE, cont)
    case _ => throw new IllegalArgumentException("The body has to be a GPB")
  }

  override def fromBody(body: TypedInput, `type`: Type): AnyRef = {
    if (!`type`.isInstanceOf[Class[_]]) {
      throw new IllegalArgumentException("Expected a raw Class<?> but was " + `type`)
    }
    val c = `type`.asInstanceOf[Class[_]]
    if (!classOf[AbstractMessageLite].isAssignableFrom(c)) {
      throw new IllegalArgumentException("Expected a protobuf message but was " + c.getName)
    }

    val mimeType = body.mimeType
    if (!(MIME_TYPE == mimeType)) {
      throw new ConversionException("Response content type was not a proto: " + mimeType)
    }

    try {
      val parseFrom = c.getMethod("parseFrom", classOf[InputStream])
      parseFrom.invoke(null, body.in)
    }
    catch {
      case e: InvocationTargetException => throw new ConversionException(c.getName + ".parseFrom() failed", e.getCause)
      case e: NoSuchMethodException => throw new IllegalArgumentException("Expected a protobuf message but was " + c.getName)
      case e: IllegalAccessException => throw new AssertionError
      case e: IOException => throw new ConversionException(e)
    }
  }
}
