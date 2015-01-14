package twiiface.stream

import akka.actor._
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

  case class Job(request: HttpRequest, worker: ActorRef)
  var jobs = Seq.empty[Job]

  // keep it lazy for testing purposes
  lazy val io = IO(Http)

  override def receive: Actor.Receive = {
    case StreamRequest(request) =>
      val workerName = "worker:" + MurmurHash.stringHash(request.uri.toString)
      log.info("start new worker for request path: '{}' - worker name: '{}'", request.uri, workerName)
      val worker = context.actorOf(Props[StreamProcessorWorkerActor], workerName)
      context.watch(worker)
      jobs :+= Job(request, worker)
      sendTo(io).withResponsesReceivedBy(worker)(request)
    case Terminated(child) =>
      log.warning("child terminated: " + child.path.name)

      val (job :: Nil, others) = jobs.partition(_.worker == child)

      // restart
      self ! StreamRequest(job.request)

      // update jobs
      jobs = others
    case _ =>
  }
}
