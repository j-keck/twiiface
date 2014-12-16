package twiiface.model

case class TwitterUser(id: Long, name: String, description: String, location: String)


case class TwitterTweet(id: BigInt, user: TwitterUser, text: String)