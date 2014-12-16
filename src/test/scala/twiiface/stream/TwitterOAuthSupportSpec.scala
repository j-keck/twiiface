package twiiface.stream

import org.scalatest.{FlatSpec, Matchers}
import spray.client.pipelining._
import spray.http.{FormData, HttpRequest}
import spray.httpx.RequestBuilding.Get
import twiiface.TwiifaceConfigMock


class TwitterOAuthSupportSpec extends FlatSpec with Matchers with TwitterOAuthSupport with TwiifaceConfigMock {


  "TwitterOAuthSupport" should "add header: 'Authorization'" in {
    def hasAuthorizationHeader(req: HttpRequest) = req.headers.exists(_.is("authorization"))

    val req = Get("/")
    hasAuthorizationHeader(req) should be(false)
    hasAuthorizationHeader(req ~> authorize) should be(true)
  }

  it should "leave the body intact" in {
    val req = Post("/", FormData(Map("key1" -> "value1", "key2" -> "value2")))

    (req ~> authorize).entity should equal(req.entity)
  }

}
