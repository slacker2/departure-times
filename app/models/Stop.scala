package models

import play.api.Play.current
import java.util.Date
import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import customMongoContext._
import scala.collection.immutable.Set
import se.radley.plugin.salat._


/* 
 * A Stop can exist for multiple Agencies and Routes 
 **/

//<Stop name="Hayes St and Fillmore St" StopCode="14994"></Stop>
//<stop tag="4994" title="Hayes St & Fillmore St" lat="37.7757999" lon="-122.43083" stopId="14994"/>
case class Stop(id: String, // use the location as the key
                stopId: String, // same as StopCode
                name: String, // same as title
                agencies: Set[String],
                routes: Set[String],
                loc: GeoJSONPoint,
                tag: Option[String])


object Stop extends ModelCompanion[Stop, String] {
  val dao = new SalatDAO[Stop, String](collection = mongoCollection("stops")) {}

  def findOneByStopId(stopId: String): Option[Stop] = dao.findOne("stopId" $eq stopId)

  def getAllStops(): List[Stop] = dao.find(MongoDBObject()).toList

  def getStopsNear(long: Double, lat: Double, distance: Int): List[Stop] = {
    dao.find(MongoDBObject("loc" -> 
               MongoDBObject("$near" -> 
                 MongoDBObject("$geometry" -> 
                   MongoDBObject("type" -> "Point", "coordinates" -> List(long,lat)),
                   "$maxDistance" -> distance
                 )
               )
             )).toList

  }

  def addAgencyAndRouteToStop(stop: Stop, agency: Agency, route: Route) = {
    val updatedAgencyList = stop.agencies + agency.id
    val updatedRouteList = stop.routes + route.id
    dao.update("_id" $eq stop.id, $set("agencies" -> updatedAgencyList, "routes" -> updatedRouteList), false, false)
    //dao.update(MongoDBObject("_id" -> stop.id), MongoDBObject("$set" -> MongoDBObject("agencies" -> updatedAgencyList, "routes" -> updatedRouteList)), false, false)
  }

}
