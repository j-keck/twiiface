package twiiface.stream

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{FlatSpecLike, Matchers}
import spray.http.MessageChunk


class StreamProcessorWorkerActorSpec extends TestKit(ActorSystem("test")) with FlatSpecLike with Matchers {

  "StreamProcessorWorkerActor" should "process a complete message in one 'MessageChunk'" in {
    withStreamProcessorWorkerActor { (spa, sp) =>
      spa ! MessageChunk(msg(1, "test1"))
    }
  }

  it should "process a message from two 'MessageChunk's" in {
    withStreamProcessorWorkerActor { (spa, sp) =>
      val (chunk1, chunk2) = msg(2, "test2").splitAt(20)
      spa ! MessageChunk(chunk1)
      spa ! MessageChunk(chunk2)
    }
  }

  it should "process two messages from one 'MessageChunk'" in {
    withStreamProcessorWorkerActor { (spa, sp) =>
      spa ! MessageChunk(List(msg(3, "test3"), msg(4, "test4")).mkString)
    }
  }


  private def msg(id: Int, text: String): String = {
    val user = """{"id": 1, "name": "tuser", "description": "duser", "location": ""}"""
    s"""{"id": $id, "user": $user, "text": "$text"}\r\n"""
  }

  private def withStreamProcessorWorkerActor(f: (ActorRef, StreamProcessorWorkerActor) => Unit): Unit = {
    val spActorRef = TestActorRef[StreamProcessorWorkerActor]
    val sp = spActorRef.underlyingActor
    f(spActorRef, sp)
  }
}
