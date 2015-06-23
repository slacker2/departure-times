package models

import play.api.Play.current
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import customMongoContext._
import se.radley.plugin.salat._


//<agency tag="actransit" title="AC Transit" regionTitle="California-Northern"/>
//<Agency Name="AC Transit" HasDirection="True" Mode="Bus"></Agency>
case class Route(id: ObjectId = new ObjectId,
                 name: String, // same as title
                 code: String, // same as tag
                 api: String, 
                 agency: ObjectId 
                 )


object Route extends ModelCompanion[Route, ObjectId] {
  val dao = new SalatDAO[Route, ObjectId](collection = mongoCollection("routes")) {}

  def getAllRoutes(): List[Route] = dao.find(MongoDBObject()).toList

}
