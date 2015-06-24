package models

import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import customMongoContext._
import play.api.libs.json._
import play.api.Play.current
import scala.concurrent.Future
import scala.xml.Elem
import se.radley.plugin.salat._


object AvailableAPIDomain extends Enumeration {
  //val API511 = "http://services.my511.org/"
  val APINextBus = "http://www.nextbus.com/"

  //def apiList = List("http://services.my511.org/", "http://www.nextbus.com/")
  def apiList = List("http://www.nextbus.com/")
}

case class APIProvider (id: String)

object APIProvider extends ModelCompanion[APIProvider, String] {
  val dao = new SalatDAO[APIProvider, String](collection = mongoCollection("apiproviders")) {}

  def getPredictedDepartureTimesForStop(stop: Stop): Future[JsValue] = {
    val agency = Agency.findOneById(id = stop.agencies.head)
    agency.get.api match {
      case AvailableAPIDomain.APINextBus => APINextBus.getPredictedDepartureTimesForStop(agency.get, stop)
      //case AvailableAPIDomain.API511 => API511.populateAllAgencies()
      //case _ => threw new InvalidAPITypeException
    }
  }
  
  def populateEverything() = {
    createAllAPIProviders

    dao.find(MongoDBObject()).toList.foreach { api =>
      populateAllAgenciesForAPI(api)
      Thread.sleep(200)
      val agenciesForCurrentAPI = Agency.dao.find("api" $eq api.id).toList
      agenciesForCurrentAPI.map { agency =>
        populateAllRoutesForAgency(agency)
        Thread.sleep(200)
        val routesForCurrentAgency = Route.dao.find("agency" $eq agency.id).toList
        routesForCurrentAgency.map { route =>
          populateAllStopsForRoute(route)
        }
      }
    }
  }

  private def createAllAPIProviders() = {
    AvailableAPIDomain.apiList.foreach { api =>
      val currentAPIProvider = APIProvider(api)
      dao.save(currentAPIProvider)
    }
  }

  private def populateAllAgenciesForAPI(api: APIProvider) = {
    api.id match {
      case AvailableAPIDomain.APINextBus => APINextBus.populateAllAgencies()
      //case AvailableAPIDomain.API511 => API511.populateAllAgencies()
      //case _ => threw new InvalidAPITypeException
    }
  }

  private def populateAllRoutesForAgency(agency: Agency) = {
    agency.api match {
      case AvailableAPIDomain.APINextBus => APINextBus.populateAllRoutesForAgency(agency)
      //case AvailableAPIDomain.API511 => API511.populateAllRoutesForAgency(agency)
      //case _ => threw new InvalidAPITypeException
    }
  }

  private def populateAllStopsForRoute(route: Route) = {
    route.api match {
      case AvailableAPIDomain.APINextBus => APINextBus.populateAllStopsForRoute(route)
      //case AvailableAPIDomain.API511 => API511.populateAllStopsForRoute(route)
      //case _ => threw new InvalidAPITypeException
    }
  }
}

