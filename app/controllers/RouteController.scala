package controllers

import models.Route
import play.api._
import play.api.mvc._


object RouteController extends Controller {

  def listAllRoutes = Action {
    val routes = Route.getAllRoutes()
    Ok(routes.toString)
  }

}
