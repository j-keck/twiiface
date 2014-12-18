package twiiface.stream

import akka.actor.Status.Failure
import akka.actor._
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.concurrent.Eventually
import org.scalatest.{FlatSpecLike, Matchers}
import spray.http.{HttpMethods, HttpRequest}
import twiiface.stream.StreamProcessorActor.StreamRequest

class StreamProcessorActorSpec extends TestKit(ActorSystem("test")) with FlatSpecLike with Matchers with Eventually{

  "StreamProcessorActor" should "start a worker child if it receives a 'StreamRequest' message" in {
    // FIXME: get rid of this!!
    var done = false

    val req = HttpRequest(method = HttpMethods.GET, uri = "/path/to/resource?k1=v1&k2=v2")
    val streamProcessorActor = TestActorRef(streamProcessorActorWrapper { (msg, child) =>
      msg should equal(req)
      child.path.name should startWith("worker:")
      done = true
    })
    streamProcessorActor ! StreamRequest(req)

    eventually{ done should be(true)}
  }

  it should "restart the worker on error" in {
    var done = false

    val req = HttpRequest(method = HttpMethods.GET, uri = "/path/to/resource?k1=v1&k2=v2")
    val streamProcessorActor = TestActorRef(streamProcessorActorWrapper { (msg, child) =>
      child ! Failure(new Exception("connection reset"))

      done = true
    })
    streamProcessorActor ! StreamRequest(req)

    eventually{ done should be(true)}
  }

  private def streamProcessorActorWrapper(onMsg: ((Any, ActorRef) => Unit)) = new StreamProcessorActor {
    override lazy val io = context.actorOf(Props(new Actor {
      override def receive: Receive = {
        case msg => onMsg(msg, sender())
      }
    }))
  }

}
