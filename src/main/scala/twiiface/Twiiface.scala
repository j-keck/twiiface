package twiiface

import akka.actor._
import twiiface.search.TwitterSearch
import twiiface.stream.TwitterStream

import scala.io.StdIn


object Twiiface extends App with TwiifaceConfig {

  implicit val system = ActorSystem("twiiface")
  import system.dispatcher

  system.eventStream.subscribe(system.actorOf(DeadLetterLoggerActor.props), classOf[DeadLetter])

  val CmdR = """(\w+)\s+(.+)""".r
  while(true){
    val cmd = StdIn.readLine("what?\n")
    cmd match {
      case CmdR("user", name) => TwitterSearch.searchUser(name).foreach(println)
      case CmdR("search", query) => TwitterSearch.searchTweets(query).foreach(println)
      case CmdR("stream", query) => TwitterStream.byTag(query, (tag, tweet) => println(s"$tag: ${tweet.text}"))
      case CmdR(cmd, _) => println(s"unknown cmd: '$cmd' - example: [user|search|stream] <WHAT>'")
    }
  }
}


object DeadLetterLoggerActor {
  def props = Props[DeadLetterLoggerActor]
}
class DeadLetterLoggerActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case DeadLetter(msg, from, to) =>
      log.warning(s"dead letter: from: $from, to: $to, msg: $msg")
  }
}