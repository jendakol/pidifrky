import java.util.concurrent.{Executors, ForkJoinPool}

import annots.{BlockingExecutor, CallbackExecutor}
import utils.GuiceConfiguration

import scala.concurrent.ExecutionContext

/**
 * @author Jenda Kolena, kolena@avast.com
 */
class AppModule extends GuiceConfiguration {

  lazy val callbackExecutor = ExecutionContext.fromExecutorService(new ForkJoinPool(Runtime.getRuntime.availableProcessors() * 2))
  //TODO
  lazy val blockingExecutor = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())

  override def configure(): Unit = {
    super.configure()

    bind(classOf[ExecutionContext]).annotatedWith(classOf[CallbackExecutor]).toInstance(callbackExecutor)
    bind(classOf[ExecutionContext]).annotatedWith(classOf[BlockingExecutor]).toInstance(blockingExecutor)

  }
}
