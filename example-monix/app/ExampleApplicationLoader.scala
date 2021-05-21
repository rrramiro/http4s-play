import play.api.{ApplicationLoader, BuiltInComponentsFromContext, LoggerConfigurator}
import play.api.ApplicationLoader.Context
import play.api.routing.Router
import play.api.mvc.EssentialFilter
import com.softwaremill.macwire._
import cats.effect._
import monix.execution.Scheduler
import router.Routes

class ExampleApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach {
      _.configure(context.environment, context.initialConfiguration, Map.empty)
    }

    new ExampleComponents(context).application
  }

}

class ExampleComponents(context: Context) extends BuiltInComponentsFromContext(context) {
  implicit val monixScheduler: Scheduler = Scheduler(executionContext)

  lazy val blocker: Blocker = Blocker.liftExecutionContext(executionContext)
  lazy val http4sRouter = wire[controllers.Http4sRouter]

  lazy val mainController = wire[controllers.Main]

  def createRoutes(prefix: String) = wire[Routes]

  override def router: Router = createRoutes("/")

  override def httpFilters: Seq[EssentialFilter] = Seq()

}
