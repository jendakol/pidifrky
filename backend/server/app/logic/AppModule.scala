package logic

import java.util.Calendar
import java.util.concurrent.{Executors, ForkJoinPool, TimeUnit}

import annots.{BlockingExecutor, CallbackExecutor}
import controllers.device.ExecutionContexts
import utils.{GuiceConfiguration, Logging}

import scala.concurrent.ExecutionContext

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class AppModule extends GuiceConfiguration with Logging {

  import AppModule._

  val initialDelay: Long = {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, 1)
    cal.set(Calendar.HOUR_OF_DAY, 2)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    //set time to 2AM of next day
    (cal.getTimeInMillis - System.currentTimeMillis()) / (1000 * 60)
  }

  Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable {
    Logger.info("12h cron initialized")

    override def run(): Unit = {

    }
  }, initialDelay, 24 * 60, TimeUnit.MINUTES)


  override def configure(): Unit = {
    super.configure()

    bind(classOf[ExecutionContext]).annotatedWith(classOf[CallbackExecutor]).toInstance(callbackExecutor)
    bind(classOf[ExecutionContext]).annotatedWith(classOf[BlockingExecutor]).toInstance(blockingExecutor)
    bind(classOf[ExecutionContexts]).toInstance(ExecutionContexts(callbackExecutor, blockingExecutor))

  }
}

object AppModule {
  implicit lazy val callbackExecutor = ExecutionContext.fromExecutorService(new ForkJoinPool(Runtime.getRuntime.availableProcessors() * 2))
  //TODO
  lazy val blockingExecutor = ExecutionContext.fromExecutorService(Executors.newCachedThreadPool())
}
