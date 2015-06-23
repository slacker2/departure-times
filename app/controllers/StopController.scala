package controllers

import models.Stop
import play.api._
import play.api.mvc._


object StopController extends Controller {

  def listAllStops = Action {
    val stops = Stop.getAllStops()
    Ok(stops.toString)
  }

}
