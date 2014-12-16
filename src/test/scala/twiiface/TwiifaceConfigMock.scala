package twiiface

import com.typesafe.config.ConfigFactory

trait TwiifaceConfigMock extends TwiifaceConfig {

  override lazy val config = ConfigFactory.parseString(
    """
      |twitter {
      |    consumer-key = "consumer-key-000"
      |    consumer-secret = "consumer-secret-000"
      |    token-value = "token-value-000"
      |    token-secret = "token-secret-000"
      |}
    """.stripMargin)

}
