package org.http4s.server.play

import akka.stream.Materializer
import cats.effect._
import play.api._
import play.api.mvc.{Result, Results}
import play.api.routing.{Router, SimpleRouter}
import play.api.routing.sird._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.TestSuite
import org.scalatestplus.play.components.OneServerPerSuiteWithComponents
import play.api.test.FakeRequest

import scala.concurrent.Future

class PlayServerSpec extends AnyFunSuite with OneServerPerSuiteWithComponents with TestSuite {

  override lazy val components: BuiltInComponents = new BuiltInComponentsFromContext(context)
    with NoHttpFiltersComponents {
    implicit val contextShift: ContextShift[IO] = IO.contextShift(executionContext)

    val http4sRouter: Router = new SimpleRouter with org.http4s.dsl.Http4sDsl[IO] {
      import org.http4s.implicits._

      val exampleService = org.http4s.HttpRoutes.of[IO] { case GET -> _ =>
        Ok(s"Hello World!")
      }

      override def routes =
        PlayRouteBuilder.httpAppToRoutes[IO](exampleService.orNotFound, executionContext)
    }

    val playRouter: Router = Router.from { case GET(p"/") =>
      defaultActionBuilder {
        Results.Ok("success!")
      }
    }

    lazy val router: Router = playRouter.orElse(http4sRouter.withPrefix("/hello"))

    override lazy val configuration: Configuration =
      Configuration().withFallback(context.initialConfiguration)

  }

  implicit lazy val materializer: Materializer = components.materializer

  test("play route") {
    import play.api.test.Helpers._
    val Some(result: Future[Result]) = route(app, FakeRequest(GET, "/"))
    assert(contentAsString(result) == "success!")
  }

  test("http4s route") {
    import play.api.test.Helpers._
    val Some(result: Future[Result]) = route(app, FakeRequest(GET, "/hello/"))
    assert(contentAsString(result) == "Hello World!")
  }
}
