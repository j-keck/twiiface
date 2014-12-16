package twiiface

import akka.actor.ActorSystem
import twiiface.search.TwitterSearch
import twiiface.stream.TwitterStream


object Twiiface extends App with TwiifaceConfig {

  implicit val system = ActorSystem("twiiface")
  import system.dispatcher

  // search user
  TwitterSearch.searchUser("jhyphenkeck").foreach(println)

  // search tweets
  TwitterSearch.searchTweets("scala").foreach(println)

  // streaming
  TwitterStream.byTag("scala")
}
