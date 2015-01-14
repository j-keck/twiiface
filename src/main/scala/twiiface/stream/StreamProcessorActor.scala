package twiiface.stream

import akka.actor._
import akka.io.IO
import spray.can.Http
import spray.client.pipelining._
import spray.http.HttpRequest
import twiiface.stream.StreamProcessorActor.StreamRequest
import twiiface._

object StreamProcessorActor {

  case class StreamRequest(tag: String, request: HttpRequest, callback: StreamCallback)

}

class StreamProcessorActor extends Actor with ActorLogging with TwitterJsonProtocol {

  import context.system

  case class Job(streamRequest: StreamRequest, request: HttpRequest, worker: ActorRef)
  var jobs = Seq.empty[Job]

  // keep it lazy for testing purposes
  lazy val io = IO(Http)

  override def receive: Actor.Receive = {
    case streamRequest @ StreamRequest(tag, request, callback) =>

      val workerName = "worker:" + tag.encode
      log.info("start new worker - worker name: '{}'", workerName)
      val worker = context.actorOf(StreamProcessorWorkerActor.props(callback.curried(tag)), workerName)
      context.watch(worker)
      jobs :+= Job(streamRequest, request, worker)
      sendTo(io).withResponsesReceivedBy(worker)(request)
    case Terminated(child) =>
      log.warning("child terminated: " + child.path.name)

      val (job :: Nil, others) = jobs.partition(_.worker == child)

      // restart
      self ! job.streamRequest

      // update jobs
      jobs = others
    case _ =>
  }
}
