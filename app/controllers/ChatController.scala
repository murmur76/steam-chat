package controllers

import javax.inject._
import actors.UserActor
import akka.actor.ActorSystem
import events._
import events.JsonFormats._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current

@Singleton
class ChatController @Inject() (system: ActorSystem) extends Controller {

  def socket = WebSocket.acceptWithActor[InEvent, JsValue] { request => out =>
    UserActor.props(system, out)
  }

}
