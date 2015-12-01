package actors

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Put
import akka.event.LoggingReceive
import events._
import events.JsonFormats._
import models.JsonFormats._
import models.Chat

import scala.concurrent._

class ChatRoomActor(chatRoomId: String) extends Actor with ActorLogging {
  var participantActors = Map[Long, ActorRef]().empty
  val mediator = DistributedPubSub(context.system).mediator

  override def preStart = {
    mediator ! Put(self)
  }

  def subscribe(userId: Long, actor: ActorRef) = {
    if (!this.participantActors.contains(userId)) {
      this.participantActors += (userId -> actor)
    }
  }

  def unsubscribe(userId: Long) = {
    this.participantActors -= userId
  }

  def receive = LoggingReceive {
    case SubscribeEvent(userId, _) => subscribe(userId, sender)
    case UnsubscribeEvent(userId) => unsubscribe(userId)
    case chat: Chat => this.participantActors.values.foreach(_ ! chat)
  }
}

object ChatRoomActor {
  def props(chatRoomId: String) = Props(new ChatRoomActor(chatRoomId))
}


