package twiiface.search

import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http.OAuth2BearerToken
import spray.httpx.SprayJsonSupport._
import spray.httpx.encoding._
import twiiface.{TwitterJsonProtocol, _}
import twiiface.model._

import scala.concurrent.Future

object TwitterSearch extends TwitterJsonProtocol {

  type Tweets = Seq[TwitterTweet]

  def searchUser(name: String)(implicit system: ActorSystem): Future[TwitterUser] = {
    import system.dispatcher

    TwitterToken.requestToken.flatMap { token =>
      val pipeline = addCredentials(OAuth2BearerToken(token)) ~>
        encode(Gzip) ~>
        sendReceive ~>
        decode(Deflate) ~>
        unmarshal[TwitterUser]

      pipeline(Get(s"https://api.twitter.com/1.1/users/show.json?screen_name=$name"))
    }
  }


  def searchTweets(query: String)(implicit system: ActorSystem): Future[Tweets] = {
    import system.dispatcher

    TwitterToken.requestToken.flatMap { token =>
      val pipeline = addCredentials(OAuth2BearerToken(token)) ~>
        encode(Gzip) ~>
        sendReceive ~>
        decode(Deflate) ~>
        unmarshal[Tweets]

      pipeline(Get(s"https://api.twitter.com/1.1/search/tweets.json?q=${query.encode}&lang=en&count=10"))
    }
  }


}
