package controllers

import play.api.mvc._

class Main(cc: ControllerComponents) extends AbstractController(cc) {

  def index: Action[AnyContent] =
    Action {
      Ok("It works!")
    }

}
