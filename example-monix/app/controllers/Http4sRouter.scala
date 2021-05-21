package controllers
import cats.effect._
import org.http4s.server.play.PlayRouteBuilder
import org.http4s.implicits._
import com.example.http4s.ExampleService
import monix.eval.Task
import monix.execution.Scheduler
import org.http4s.HttpApp
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter

class Http4sRouter(blocker: Blocker)(implicit
    scheduler: Scheduler
) extends SimpleRouter {
  val httpApp: HttpApp[Task] = new ExampleService[Task](blocker).routes.orNotFound
  override def routes: Routes = PlayRouteBuilder.httpAppToRoutes[Task](httpApp, scheduler)
}
