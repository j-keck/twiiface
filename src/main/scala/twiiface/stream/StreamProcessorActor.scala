package twiiface.stream

import akka.actor.Actor
import spray.http.MessageChunk
import spray.json.JsonParser
import twiiface.TwitterJsonProtocol

import scala.collection.mutable


class StreamProcessorActor extends Actor {

  private var buffer = new mutable.StringBuilder()

  def receive = {
    case MessageChunk(entity, _) =>
      val chunk = entity.asString
      // twitter sends every 30seconds '\r\n' if no data available
      // to keep the connection open
      if (chunk != "\r\n")
        processChunk(chunk)
    case _ =>
  }

  def processChunk(chunk: String): Unit = {
    buffer.append(chunk)

    var idx = buffer.indexOf('\r')
    while (idx != -1) {
      val (tw, rest) = buffer.splitAt(idx + 1) // +1: inkl \n
      processTweet(tw.mkString)

      buffer = rest
      idx = buffer.indexOf('\r')
    }
  }

  def processTweet(jsonStr: String): Unit = {
    println(TwitterJsonProtocol.extractTweet(JsonParser(jsonStr).asJsObject))
  }
}
