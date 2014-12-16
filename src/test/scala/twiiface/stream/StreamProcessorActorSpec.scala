package twiiface.stream

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{FlatSpecLike, Matchers}
import spray.http.MessageChunk


class StreamProcessorActorSpec extends TestKit(ActorSystem("test")) with FlatSpecLike with Matchers {

  "StreamProcessorActor" should "process a complete message in one 'MessageChunk'" in {
    withStreamProcessorActor { (spa, sp) =>
      spa ! MessageChunk(msg(1, "test1"))
    }
  }

  it should "process a message from two 'MessageChunk's" in {
    withStreamProcessorActor { (spa, sp) =>
      spa ! MessageChunk( s"""{"id_str": "2",""")
      spa ! MessageChunk( s""" "text": "test2"}\r\n""")
    }
  }

  it should "process two messages from one 'MessageChunk'" in {
    withStreamProcessorActor { (spa, sp) =>
      spa ! MessageChunk(List(msg(3, "test3"), msg(4, "test4")).mkString)
    }
  }


  private def msg(id: Int, text: String): String = s"""{"id_str": "$id", "text": "$text"}\r\n"""

  private def withStreamProcessorActor(f: (ActorRef, StreamProcessorActor) => Unit): Unit = {
    val spActorRef = TestActorRef[StreamProcessorActor]
    val sp = spActorRef.underlyingActor
    f(spActorRef, sp)
  }
}
