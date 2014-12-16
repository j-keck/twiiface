package twiiface

import spray.httpx.unmarshalling.{ContentExpected, Deserialized, MalformedContent}
import spray.json._
import twiiface.model.{TwitterTweet, TwitterUser}

object TwitterJsonProtocol {

  def extractUser(jsonObj: JsObject): Deserialized[TwitterUser] =
    jsonObj.getFields("id", "name", "description", "location") match {
      case Seq(JsNumber(id), JsString(name), JsString(description), JsString(location)) =>
        Right(TwitterUser(id.toLong, name, description, location))
      case Seq(JsNumber(id), JsString(name), JsNull, JsString(location)) =>
        // FIXME: description is 'null' when empty (location is '""' when empty)
        Right(TwitterUser(id.toLong, name, "", location))
      case Seq() => Left(ContentExpected)
      case _ =>
        Left(MalformedContent("invalid json"))
    }

  def extractTweet(jsonObj: JsObject): Deserialized[TwitterTweet] =
    jsonObj.getFields("id", "user", "text") match {
      case Seq(JsNumber(id), userJson@JsObject(_), JsString(text)) =>
        extractUser(userJson).right.map(TwitterTweet(id.toBigInt, _, text))
      case _ =>
        Left(MalformedContent("invalid json"))
    }


  def extractTweets(jsonObj: JsObject): Deserialized[Seq[TwitterTweet]] = jsonObj.getFields("statuses") match {
    case Seq(JsArray(statuses)) =>
      Right(statuses.map(e => extractTweet(e.asJsObject)).collect {
        case e if e.isRight => e.right.get
      })
    case _ => Left(MalformedContent("invalid json"))
  }

}
