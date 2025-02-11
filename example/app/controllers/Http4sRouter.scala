package controllers
import cats.effect._
import org.http4s.server.play.PlayRouteBuilder
import org.http4s.implicits._
import com.example.http4s.ExampleService
import org.http4s.HttpApp
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter

import scala.concurrent.ExecutionContext

class Http4sRouter(blocker: Blocker)(implicit
    executionContext: ExecutionContext,
    contextShift: ContextShift[IO],
    timer: Timer[IO]
) extends SimpleRouter {
  val httpApp: HttpApp[IO] = new ExampleService[IO](blocker).routes.orNotFound
  override def routes: Routes = PlayRouteBuilder.httpAppToRoutes[IO](httpApp, executionContext)
}
