package twiiface

import akka.actor.ActorSystem
import twiiface.stream.TwitterStream


object Twiiface extends App with TwiifaceConfig {

  implicit val system = ActorSystem("twiiface")
  TwitterStream.byTag("scala")
}
