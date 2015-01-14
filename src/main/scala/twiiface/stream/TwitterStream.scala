package twiiface.stream

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import spray.client.pipelining._
import spray.http._
import twiiface.model.TwitterTweet
import twiiface.{TwiifaceConfig, _}
import twiiface.stream.StreamProcessorActor.StreamRequest

object TwitterStream extends TwitterStream with TwiifaceConfig

trait TwitterStream extends TwitterOAuthSupport {
  self: TwiifaceConfig =>


  def byTag(tag: Tag, callback: StreamCallback)(implicit system: ActorSystem) = {
    import system.log

    val request = Post("https://stream.twitter.com/1.1/statuses/filter.json",
        FormData(Map("track" -> tag, "language" -> "en"))) ~>
      authorize

    val streamProcessor = system.actorOf(Props[StreamProcessorActor])
    streamProcessor ! StreamRequest(tag, request, callback)
  }
}

