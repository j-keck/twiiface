package twiiface

import com.typesafe.config.ConfigFactory

trait TwiifaceConfig {

  protected[this] lazy val config = {
    val appPrivateConfig = ConfigFactory.load("application-private.conf")
    val appConfig = ConfigFactory.load("application.conf")

    // configuration in 'application-private.conf' has higher priority
    appPrivateConfig.withFallback(appConfig).getConfig("twiiface")
  }


  object TwitterConfig {
    private lazy val twitter = config.getConfig("twitter")
    lazy val tokenServerUrl = twitter.getString("token-server-url")

    lazy val consumerKey = twitter.getString("consumer-key")
    lazy val consumerSecret = twitter.getString("consumer-secret")

    lazy val tokenValue = twitter.getString("token-value")
    lazy val tokenSecret = twitter.getString("token-secret")
  }
}
