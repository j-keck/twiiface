package twiiface.stream

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.apache.commons.codec.binary.Base64
import spray.http.HttpHeaders.RawHeader
import spray.http._
import twiiface._

import scala.collection.immutable.TreeMap
import scala.language.postfixOps

trait TwitterOAuthSupport {
  self: TwiifaceConfig =>

  type Bytes = Array[Byte]

  private val crypt = initCrypt(TwitterConfig.consumerSecret, TwitterConfig.tokenSecret)

  def authorize(httpRequest: HttpRequest): HttpRequest = {

    val oauthParams = TreeMap(
      "oauth_consumer_key" -> TwitterConfig.consumerKey,
      "oauth_signature_method" -> "HMAC-SHA1",
      "oauth_timestamp" -> (System.currentTimeMillis / 1000).toString,
      "oauth_nonce" -> System.nanoTime.toString,
      "oauth_token" -> TwitterConfig.tokenValue,
      "oauth_version" -> "1.0"
    )

    // extract request parameters - needed for signature
    val orgRequestParams: Map[String, String] = httpRequest.entity match {
      case HttpEntity.NonEmpty(ContentType(MediaTypes.`application/x-www-form-urlencoded`, _), data) =>
        data.asString.split("&").map { param =>
          val Array(key, value) = param.split("=")
          key -> value
        }.toMap
      case _ => Map()
    }

    // build the signature
    val method = httpRequest.method.toString().encode
    val url = httpRequest.uri.toString().encode
    val params = oauthParams ++ orgRequestParams map { case (k, v) => s"$k=$v"} mkString "&" encode
    val sigStr = (method & url & params)
    val sig = Base64.encodeBase64String(crypt(sigStr.bytes)).encode

    // build OAuth header
    val oauth = oauthParams + ("oauth_signature" -> sig) map { case (k, v) => s"""$k="$v""""} mkString ", "

    // add OAuth header to request
    httpRequest.withHeaders(List(RawHeader("Authorization", "OAuth " + oauth)))
  }


  private def initCrypt(consumerSecret: String, tokenSecret: String): Bytes => Bytes = {
    val key = (TwitterConfig.consumerSecret.encode & TwitterConfig.tokenSecret.encode)
    val keySpec = new SecretKeySpec(key.bytes, "HmacSHA1")
    val mac = Mac.getInstance("HmacSHA1")

    { b: Bytes =>
      mac.init(keySpec)
      val bc = mac.doFinal(b)
      mac.reset()
      bc
    }
  }
}
