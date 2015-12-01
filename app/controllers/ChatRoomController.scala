package controllers

import javax.inject.Inject

import models.JsonFormats._
import models._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import play.modules.reactivemongo.json._
import play.api.libs.functional.syntax._
import play.api.mvc._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future

class ChatRoomController @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {

  def userCollection = db.collection[JSONCollection]("users")
  def chatRoomCollection = db.collection[JSONCollection]("chatrooms")

  def create = Action.async(parse.json) { request =>
    request.body.validate[ChatRoom].map { value =>
      val chatRoomWithObjectId = new ChatRoom(Option(BSONObjectID.generate), value.participants, value.createdAt)
      chatRoomCollection.insert(chatRoomWithObjectId).map { _ =>
        Ok(Json.obj(
          "id" -> chatRoomWithObjectId._id.get.stringify,
          "participants" -> chatRoomWithObjectId.participants,
          "createdAt" -> chatRoomWithObjectId.createdAt.toString
        ))
      }
    }.getOrElse(Future.successful(BadRequest))
  }
}
