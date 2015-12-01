package models

import org.joda.time.DateTime
import play.api.libs.json.JsValue
import reactivemongo.bson.BSONObjectID

case class User(
  _id: Option[BSONObjectID],
  userId: Long,
  firstName: String,
  lastName: String,
  profileImage: String,
  gender: String,
  chatRooms: Option[List[String]]
)

case class ChatRoom(
  _id: Option[BSONObjectID],
  participants: List[Long],
  createdAt: DateTime
)

case class Chat(
  _id: BSONObjectID,
  userId: Long,
  chatRoomId: String,
  message: String,
  createdAt: DateTime
)

object JsonFormats {
  import play.api.libs.json._
  import play.modules.reactivemongo.json._
  import play.api.mvc.WebSocket.FrameFormatter

  implicit val chatFormat = Json.format[Chat]
  implicit val chatRoomFormat = Json.format[ChatRoom]
  implicit val userFormat = Json.format[User]
}

