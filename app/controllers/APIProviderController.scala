package controllers

import models.APIProvider
import play.api._
import play.api.mvc._

object APIProviderController extends Controller {

  def populateEverything = Action {
    APIProvider.populateEverything()
    Ok("Population under way!")
  }

}
