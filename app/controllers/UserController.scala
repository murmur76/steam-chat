package controllers

import models._
import models.JsonFormats._
import javax.inject.Inject
import play.api.mvc._
import play.api.libs.json._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.{ReactiveMongoComponents, MongoController, ReactiveMongoApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import reactivemongo.bson.BSONObjectID

class UserController @Inject() (val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {

  def userCollection = db.collection[JSONCollection]("users")

  def create = Action.async(parse.json) { request =>
    request.body.validate[User].map { value =>
      val userWithObjectId = new User(Option(BSONObjectID.generate), value.userId, value.firstName, value.lastName, value.gender, value.profileImage, Option(List()))
      userCollection.insert(userWithObjectId).map { _ =>
        Ok(Json.obj(
          "id" -> userWithObjectId._id.get.stringify,
          "userId" -> userWithObjectId.userId,
          "firstName" -> userWithObjectId.firstName,
          "lastName" -> userWithObjectId.lastName,
          "gender" -> userWithObjectId.gender,
          "profileImage" -> userWithObjectId.profileImage,
          "chatRooms" -> Json.toJson(userWithObjectId.chatRooms)
        ))
      }
    }.getOrElse(Future.successful(BadRequest))
  }
}
