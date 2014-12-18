package twiiface.stream

import akka.actor.{Actor, ActorLogging, Props, Terminated}
import akka.io.IO
import akka.routing.MurmurHash
import spray.can.Http
import spray.client.pipelining._
import spray.http.HttpRequest
import twiiface.TwitterJsonProtocol
import twiiface.stream.StreamProcessorActor.StreamRequest

object StreamProcessorActor {

  case class StreamRequest(request: HttpRequest)

}

class StreamProcessorActor extends Actor with ActorLogging with TwitterJsonProtocol {

  import context.system

  // keep it lazy for testing purposes
  lazy val io = IO(Http)

  override def receive: Actor.Receive = {
    case StreamRequest(request) =>
      val workerName = "worker:" + MurmurHash.stringHash(request.uri.toString)
      log.info("start new worker for request path: '{}' - worker name: '{}'", request.uri, workerName)
      val worker = context.actorOf(Props[StreamProcessorWorkerActor], workerName)
      sendTo(io).withResponsesReceivedBy(worker)(request)
    case Terminated(child) =>
      log.warning("child terminated: " + child.path.name)
    case _ =>
  }
}
