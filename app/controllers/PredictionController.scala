package controllers

import models._
import play.api._
import play.api.libs.ws._
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import play.api.mvc._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PredictionController extends Controller {

  def index() = Action {
    Ok(views.html.predictions())
  }

  def predictDepartureTimesNearUser() = Action.async {
    implicit request =>

      try {

        // (90.0, 0.0) are the coordinates for Antartica
        val lon = request.queryString.get("lon").map { s => s.head.toDouble }.getOrElse(90.0)
        val lat = request.queryString.get("lat").map { s => s.head.toDouble }.getOrElse(0.0)

        // If the clientside API was able to successfully geolocate
        if(lon != 90.0 && lat != 0.0) {
          predictDepartureTimesNear(lon, lat, 200).map { p => Ok(p.toString) }
        } else {
          WS.url("http://52.4.157.228:8080/csv/" + request.remoteAddress).get().flatMap { response =>
            // (0)ip,(1)country_code,(2)country_name,(3)region_code,
            // (4)region_name,(5)city,(6)zip_code,(7)time_zone,(8)latitude,
            // (9)longitude,(10)metro_code
            val locationArray = response.body.split(",")
            val latitude = locationArray(8).toDouble
            val longitude = locationArray(9).toDouble
            predictDepartureTimesNear(longitude, latitude, 200).map { p => Ok(p.toString) }
          }
        }
      } catch {
        case _: Throwable => Future(BadRequest)
      }
  }

  def predictDepartureTimesNearQuery() = Action.async {
    implicit request =>
      try {
        val lon = request.queryString.get("lon").map { s => s.head.toDouble }.get
        val lat = request.queryString.get("lat").map { s => s.head.toDouble }.get
        val rad = request.queryString.get("rad").map { s => s.head.toInt }.getOrElse(100)
        predictDepartureTimesNear(lon, lat, rad).map { p => Ok(p.toString) }
      } catch {
        case _: Throwable => Future(BadRequest)
      }
  }

  //private def predictDepartureTimesNear(lon: Double, lat: Double, rad: Int) = Action.async {
  private def predictDepartureTimesNear(lon: Double, lat: Double, rad: Int) = {
    val stops = Stop.getStopsNear(lon, lat, rad)

    val predictionList = stops.map { stop =>
      APIProvider.getPredictedDepartureTimesForStop(stop)
    }

    val flattenedPredictions = predictionList.flatMap { predictionSet =>
      predictionSet.map { predictionFuture =>
        predictionFuture
      }
    }

    Future.sequence(flattenedPredictions)

  }

}
