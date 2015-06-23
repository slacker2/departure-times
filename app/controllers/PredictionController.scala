package controllers

import models._
import play.api._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object PredictionController extends Controller {

  def predictDepartureTimesNear(lon: String, lat: String, distance: String) = Action.async {
    val stops = Stop.getStopsNear(lon.toDouble, lat.toDouble, distance.toInt)

    val predictionList = stops.map { stop =>
      APIProvider.getPredictedDepartureTimesForStop(stop)
    }

    val flattenedPredictions = predictionList.flatMap { predictionSet =>
      predictionSet.map { predictionFuture =>
        predictionFuture
      }
    }

    for {
      predictions <- Future.sequence(flattenedPredictions)
    } yield {
      Ok(predictions.toString)
    }
  }

}
