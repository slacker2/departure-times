package models

import com.novus.salat._
import com.novus.salat.annotations._
import com.novus.salat.dao._
import com.mongodb.casbah.Imports._
import customMongoContext._
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import scala.collection.immutable.Set
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.Elem
import se.radley.plugin.salat._


object APINextBus {

  val domain = "http://www.nextbus.com/"
  val agencyListCommand = "http://webservices.nextbus.com/service/publicXMLFeed?command=agencyList"
  val routeListCommand = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeList"
  val routeConfigCommand = "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig"
  val predictionsCommand = "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions"

  def getPredictedDepartureTimesForStop(agency: Agency, stop: Stop): Set[Future[scala.xml.Elem]] = {
    val routes = stop.routes.map { routeId => Route.findOneById( id = routeId ) }.filter { route => route.get.agency == agency.id }

    routes.map { route =>
      val predictionsCommandRequest = WS.url(predictionsCommand)
        .withQueryString("a" -> agency.tag.get, "r" -> route.get.code, "s" -> stop.tag.get)
      val responseFuture = predictionsCommandRequest.get()

      responseFuture.map { response =>
        response.xml
      }
    }
  }

  def populateAllAgencies() = {
    val agencyListCommandRequest = WS.url(agencyListCommand)
    val responseFuture = agencyListCommandRequest.get()

    responseFuture.map { response =>
      // <agency tag="actransit" title="AC Transit" regionTitle="California-Northern"/>
      (response.xml \\ "agency").foreach { agencyXML =>
        val agency = Agency(new ObjectId(),
                            (agencyXML \ "@title").toString,              // name 
                            AvailableAPIDomain.APINextBus,                // api
                            AgencyType.bus,                               // mode
                            None,                                         // hasDirection
                            Some((agencyXML \ "@regionTitle").toString),  // regionTitle
                            Some((agencyXML \ "@tag").toString)           // tag
                           ) 
        Agency.save(agency)
      }
    }
  }

  def populateAllRoutesForAgency(agency: Agency) = {
    val routeListCommandRequest = WS.url(routeListCommand).withQueryString("a" -> agency.tag.get)
    val responseFuture = routeListCommandRequest.get()

    responseFuture.map { response =>
      // <route tag="F" title="F-Market & Wharves"/>
      (response.xml \\ "route").foreach { routeXML =>
        val route = Route(new ObjectId(),                   // id
                          (routeXML \ "@title").toString,   // name
                          (routeXML \ "@tag").toString,     // code
                          AvailableAPIDomain.APINextBus,    // api
                          agency.id                         // agency
                         )
        Route.save(route)
      }
    }
  }

  def populateAllStopsForRoute(route: Route) = {
    Agency.findOneById(id = route.agency).map { agency =>
      val routeConfigCommandRequest = WS.url(routeConfigCommand).withQueryString("a" -> agency.tag.get, "r" -> route.code)
      val responseFuture = routeConfigCommandRequest.get()

      responseFuture.map { response =>
        // <stop tag="5671" title="Market St & Front St" lat="37.79192" lon="-122.3981499" stopId="15671"/>
        (response.xml \\ "stop").foreach { stopXML =>
          val longitude = (stopXML \ "@lon").toString
          val latitude = (stopXML \ "@lat").toString
          val locationString = longitude + ":" + latitude
          Stop.findOneById(id = locationString) match {
            case Some(stop) => Stop.addAgencyAndRouteToStop(stop, agency, route)
            case None => {

              val location = GeoJSONPoint("Point", List(longitude.toDouble, latitude.toDouble))
              val newStop = Stop(locationString,                    // id
                                 (stopXML \ "@stopId").toString,    // stopId
                                 (stopXML \ "@title").toString,     // name
                                 Set(agency.id),                    // agencies
                                 Set(route.id),                     // routes
                                 location,                          // loc
                                 Some((stopXML \ "@tag").toString)  // tag
                                )
              Stop.save(newStop)
            }
          }
        }
      }
    }
  }
}

