package events

import play.api.libs.json._
import play.api.mvc.WebSocket.FrameFormatter


case class InEvent(action: String, data: JsValue)
case class SubscribeEvent(userId: Long, chatRoomId: String)
case class UnsubscribeEvent(userId: Long)
case class BroadcastEvent(userId: Long, message: String)


object JsonFormats {
  implicit val inEventFormat = Json.format[InEvent]
  implicit val subscriveEventFormat = Json.format[SubscribeEvent]
  implicit val unsubscriveEventFormat = Json.format[UnsubscribeEvent]
  implicit val broadcastEventFormat = Json.format[BroadcastEvent]
  implicit val inEventFrameFormatter = FrameFormatter.jsonFrame[InEvent]
}
