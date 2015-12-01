package actors

import javax.inject.Inject
import akka.actor._
import akka.pattern.ask
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.Send
import akka.event.LoggingReceive
import akka.util.Timeout
import events._
import events.JsonFormats._
import models.Chat
import models.JsonFormats._
import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.api.Play.current
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
import scala.concurrent._
import scala.util.{Success, Failure}

class UserActor(system: ActorSystem, out: ActorRef) extends Actor with ActorLogging {
  var chatRoomActor: Option[ActorRef] = None
  var userId: Long = 0
  var chatRoomId: String = ""
  val mediator = DistributedPubSub(system).mediator

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]
  def chatCollection = reactiveMongoApi.db.collection[JSONCollection]("chats")

  def registerChatRoom(chatRoomId: String): Future[ActorRef] = {
    implicit val timeout = Timeout(100, duration.MILLISECONDS)
    val future = mediator ? Send("/user/chatrooms@" + chatRoomId, Identify, localAffinity = true)
    future.map { result =>
      result.asInstanceOf[ActorIdentity].ref.getOrElse(system.actorOf(ChatRoomActor.props(chatRoomId), "chatrooms@" + chatRoomId))
    }
  }

  override def postStop() = {
    this.chatRoomActor match {
      case Some(actor) => actor ! UnsubscribeEvent(this.userId)
      case None =>
    }
  }

  def receive = LoggingReceive {
    case chat: Chat => out ! Json.toJson(chat)
    case InEvent("subscribe", data) =>
      data.validate[SubscribeEvent].map { event =>
        this.userId = event.userId
        this.chatRoomId = event.chatRoomId
        this.chatRoomActor match {
          case None => registerChatRoom(event.chatRoomId).map { actor =>
            this.chatRoomActor = Option(actor)
            actor ! event
          }
          case Some(actor) => actor ! event
        }
      }
    case InEvent("broadcast", data) =>
      data.validate[BroadcastEvent].map { event =>
        this.chatRoomActor match {
          case None => out ! Json.obj("message" -> "Not Subscribed")
          case Some(actor) =>
            val chat = Chat(BSONObjectID.generate, this.userId, this.chatRoomId, event.message, new DateTime())
            chatCollection.insert(chat).onComplete {
              case Success(v) => actor ! chat
              case Failure(e) => out ! Json.obj("message" -> "Database Error")
            }
        }
      }
  }
}

object UserActor {
  def props(system: ActorSystem, out: ActorRef) = Props(new UserActor(system, out))
}
