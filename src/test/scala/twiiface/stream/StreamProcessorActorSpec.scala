package twiiface.stream

import akka.actor.Status.Failure
import akka.actor._
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.concurrent.{PatienceConfiguration, Eventually}
import org.scalatest.time.{Span, Second}
import org.scalatest.{FlatSpecLike, Matchers}
import spray.http.{MessageChunk, HttpMethods, HttpRequest}
import twiiface.stream.StreamProcessorActor.StreamRequest

import scala.collection.mutable

class StreamProcessorActorSpec extends TestKit(ActorSystem("test")) with FlatSpecLike with Matchers with Eventually{

  "StreamProcessorActor" should "start a worker child if it receives a 'StreamRequest' message" in {
    // FIXME: get rid of this!!
    var done = false

    val req = HttpRequest(method = HttpMethods.GET, uri = "/path/to/resource?k1=v1&k2=v2")
    val streamProcessorActor = TestActorRef(streamProcessorActorWrapper { (msg, msgSender) =>
      msg should equal(req)
      msgSender.path.name should startWith("worker:")
      done = true
    })
    streamProcessorActor ! StreamRequest("test", req)

    eventually{ done should be(true)}
  }

  it should "restart the worker on error" in {
    val req = HttpRequest(method = HttpMethods.GET, uri = "/path/to/resource?k1=v1&k2=v2")

    var msgCount = 0
    val streamProcessorActor = TestActorRef(streamProcessorActorWrapper { (msg, msgSender) =>
      msg should equal(req)

      // inject Failure message
      msgSender ! Failure(new Exception("connection reset"))
      msgCount += 1
    })
    streamProcessorActor ! StreamRequest("test", req)

    eventually{ msgCount should be > 1 }
  }

  private def streamProcessorActorWrapper(onMsg: ((Any, ActorRef) => Unit)) = new StreamProcessorActor {
    override lazy val io = context.actorOf(Props(new Actor {
      override def receive: Receive = {
        case msg => onMsg(msg, sender())
      }
    }))
  }

}
