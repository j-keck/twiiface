package twiiface.stream

import akka.actor.Status.Failure
import akka.actor.{ActorLogging, Actor}
import spray.http.MessageChunk
import spray.json.JsonParser
import twiiface.TwitterJsonProtocol


import scala.collection.mutable

class StreamProcessorWorkerActor extends Actor with ActorLogging with TwitterJsonProtocol {
  private var buffer = new mutable.StringBuilder()

  def receive = {
    case MessageChunk(entity, _) =>
      val chunk = entity.asString
      // twitter sends every 30seconds '\r\n' if no data available
      // to keep the connection open
      if (chunk != "\r\n")
        processChunk(chunk)
    case Failure(reason) =>
      log.error(reason, "Failure - stop")
      context.stop(self)
    case _ =>
  }

  def processChunk(chunk: String): Unit = {
    buffer.append(chunk)

    // tweets are separated with '\r\n'
    // there are never a '\r' in a message - '\n' is possible
    var idx = buffer.indexOf('\r')
    while (idx != -1) {
      val (tw, rest) = buffer.splitAt(idx + 1) // +1: inkl \n
      processTweet(tw.mkString)

      buffer = rest
      idx = buffer.indexOf('\r')
    }
  }

  def processTweet(jsonStr: String): Unit = {
    println(TwitterTweetFormat.read(JsonParser(jsonStr)))
  }
}
