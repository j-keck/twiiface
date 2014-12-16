package twiiface.search

import akka.actor.ActorSystem
import org.apache.commons.codec.binary.Base64
import spray.client.pipelining._
import spray.http.FormData
import spray.json.DefaultJsonProtocol._
import twiiface.TwiifaceConfig

import scala.concurrent.Future

object TwitterToken extends TwitterToken with TwiifaceConfig

trait TwitterToken {
  self: TwiifaceConfig =>

  type Token = String

  private val credentials = {
    val consumerKey = TwitterConfig.consumerKey
    val consumerSecret = TwitterConfig.consumerSecret
    Base64.encodeBase64String(s"$consumerKey:$consumerSecret".getBytes)
  }

  def requestToken()(implicit system: ActorSystem): Future[Token] = {
    import spray.httpx.SprayJsonSupport._
    import system.dispatcher

    case class TokenResponse(token_type: String, access_token: String)
    implicit val tokenResponseFormat = jsonFormat2(TokenResponse)

    val pipeline = addHeader("Authorization", s"Basic $credentials") ~>
      sendReceive ~>
      unmarshal[TokenResponse]

    pipeline {
      Post(TwitterConfig.tokenServerUrl, FormData(Map("grant_type" -> "client_credentials")))
    }.map(_.access_token)
  }

}

