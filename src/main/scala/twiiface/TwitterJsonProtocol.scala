package twiiface

import spray.httpx.unmarshalling.{ContentExpected, Deserialized, MalformedContent}
import spray.json._
import twiiface.model.{TwitterTweet, TwitterUser}

trait TwitterJsonProtocol extends DefaultJsonProtocol{

  implicit object TwitterUserFormat extends RootJsonFormat[TwitterUser] {
    override def read(json: JsValue): TwitterUser =
      json.asJsObject.getFields("id", "name", "description", "location") match {
        case Seq(JsNumber(id), JsString(name), JsString(description), JsString(location)) =>
          TwitterUser(id.toLong, name, description, location)
        case Seq(JsNumber(id), JsString(name), JsNull, JsString(location)) =>
          // FIXME: description is 'null' when empty (location is '""' when empty)
          TwitterUser(id.toLong, name, "", location)
        case _ => deserializationError("invalid json")
      }

    override def write(obj: TwitterUser): JsValue = deserializationError("not implemented")
  }


  implicit object TwitterTweetFormat extends RootJsonFormat[TwitterTweet] {
    override def read(json: JsValue): TwitterTweet = {
      json.asJsObject.getFields("id", "user", "text") match {
        case Seq(JsNumber(id), jsUserObj @ JsObject(_), JsString(text)) =>
          TwitterTweet(id.toBigInt, TwitterUserFormat.read(jsUserObj), text)
      }
    }

    override def write(obj: TwitterTweet): JsValue = deserializationError("not implemented")
  }
}
