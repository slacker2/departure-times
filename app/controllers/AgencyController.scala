package controllers

import models.Agency
import play.api._
import play.api.mvc._


object AgencyController extends Controller {

  def listAllAgencies = Action {
    val agencies = Agency.getAllAgencies()
    Ok(agencies.toString)
  }

}
