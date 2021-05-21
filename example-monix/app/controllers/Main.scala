package controllers

import javax.inject.Inject
import play.api.mvc._

class Main(cc: ControllerComponents) extends AbstractController(cc) {

  def index: Action[AnyContent] =
    Action {
      Ok("It works!")
    }

}
