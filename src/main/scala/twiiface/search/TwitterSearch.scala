package twiiface.search

import akka.actor.ActorSystem
import spray.client.pipelining._
import spray.http.{HttpResponse, OAuth2BearerToken}
import spray.httpx.encoding._
import spray.httpx.unmarshalling.Deserialized
import spray.json.JsonParser
import twiiface.TwitterJsonProtocol
import twiiface.model._

import scala.concurrent.Future

object TwitterSearch {

  type SearchResult[T] = Future[Deserialized[T]]
  type Tweets = Seq[TwitterTweet]


  def searchUser(name: String)(implicit system: ActorSystem): SearchResult[TwitterUser] = {
    import system.dispatcher
    search(s"https://api.twitter.com/1.1/users/show.json?screen_name=$name").map { resp =>
      val json = JsonParser(resp.entity.asString).asJsObject
      TwitterJsonProtocol.extractUser(json)
    }
  }


  def searchTweets(query: String)(implicit system: ActorSystem): SearchResult[Tweets] = {
    import system.dispatcher
    search(s"https://api.twitter.com/1.1/search/tweets.json?q=$query&lang=en&count=100").map { resp =>
      val json = JsonParser(resp.entity.asString).asJsObject
      TwitterJsonProtocol.extractTweets(json)
    }
  }


  private def search(url: String)(implicit system: ActorSystem): Future[HttpResponse] = {
    import system.{dispatcher, log}

    def executeRequest(token: String) = {
      val pipeline = addCredentials(OAuth2BearerToken(token)) ~>
        encode(Gzip) ~>
        sendReceive ~>
        decode(Deflate)

      pipeline(Get(url))
    }

    TwitterToken.requestToken.flatMap(executeRequest)
  }


}
