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

  case class StreamRequest(tag: String, request: HttpRequest)

}

class StreamProcessorActor extends Actor with ActorLogging with TwitterJsonProtocol {

  import context.system

  case class Job(tag: String, request: HttpRequest, worker: ActorRef)
  var jobs = Seq.empty[Job]

  // keep it lazy for testing purposes
  lazy val io = IO(Http)

  override def receive: Actor.Receive = {
    case StreamRequest(tag, request) =>

      val workerName = "worker:" + tag
      log.info("start new worker - worker name: '{}'", workerName)
      val worker = context.actorOf(Props[StreamProcessorWorkerActor], workerName)
      context.watch(worker)
      jobs :+= Job(tag, request, worker)
      sendTo(io).withResponsesReceivedBy(worker)(request)
    case Terminated(child) =>
      log.warning("child terminated: " + child.path.name)

      val (job :: Nil, others) = jobs.partition(_.worker == child)

      // restart
      self ! StreamRequest(job.tag, job.request)

      // update jobs
      jobs = others
    case _ =>
  }
}
