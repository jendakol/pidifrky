package utils

import java.nio.file.{Path, Paths}

import annots.{StoragePath, ConfigProperty, ConfigPropertyImpl, StoragePathImpl}
import com.google.inject.AbstractModule
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._

/**
  * @author Jenda Kolena, kolena@avast.com
  */
class GuiceConfiguration extends AbstractModule with Logging {

  private lazy val config = ConfigFactory.load().root()

  override def configure(): Unit = {
    bindConfig(config, "")(binder())
    bindStoragePaths(config.toConfig, binder())
  }

  import java.lang.{Boolean => JBoolean}

  import com.google.inject.{Binder, Key}
  import com.typesafe.config._

  import scala.collection.JavaConversions._

  //based on: http://vastdevblog.vast.com/blog/2012/06/16/creating-named-guice-bindings-for-typesafe-config-properties/
  private def bindConfig(obj: ConfigValue, bindingPath: String)(implicit binder: Binder): Unit = obj.valueType() match {
    case ConfigValueType.OBJECT =>
      val configObj = obj.asInstanceOf[ConfigObject]
      // Bind the config from the object.
      binder
        .bind(Key.get(classOf[Config], initConfigProperty(bindingPath)))
        .toInstance(configObj.toConfig)
      // Bind any nested values.
      configObj.entrySet().foreach(me => {
        val key = me.getKey
        bindConfig(me.getValue, bindingPath + "." + key)
      })
    case ConfigValueType.LIST =>
      val values = obj.asInstanceOf[ConfigList]
      for (i <- 0 until values.size()) {
        bindConfig(values(i),
          bindingPath + "." + i.toString)
      }
    case ConfigValueType.NUMBER =>
      // Bind as string and rely on guice's conversion code when the value is used.
      binder
        .bindConstant()
        .annotatedWith(initConfigProperty(bindingPath))
        .to(obj.unwrapped()
          .asInstanceOf[Number].toString)
    case ConfigValueType.BOOLEAN =>
      binder
        .bindConstant()
        .annotatedWith(initConfigProperty(bindingPath))
        .to(obj.unwrapped()
          .asInstanceOf[JBoolean])
    case ConfigValueType.NULL =>
    // NULL values are ignored.
    case ConfigValueType.STRING =>
      binder
        .bindConstant()
        .annotatedWith(initConfigProperty(bindingPath))
        .to(obj.unwrapped()
          .asInstanceOf[String])
  }

  private def bindStoragePaths(config: Config, binder: Binder): Unit = {
    val root = config.getString("play.server.dir")

    config.getConfig("paths").entrySet().asScala.foreach { entry =>
      val key = entry.getKey
      val value = entry.getValue.unwrapped().toString

      val path = Paths.get(root, value).toAbsolutePath

      Logger.info(s"Binding StoragePath('$key') to $path")

      binder.bind(classOf[Path])
        .annotatedWith(initStoragePath(key))
        .toInstance(path)

      binder.bind(classOf[StorageDir])
        .annotatedWith(initStoragePath(key))
        .toInstance(StorageDir(path))

      binder.bindConstant()
        .annotatedWith(initStoragePath(key))
        .to(path.toString)
    }
  }

  private def initStoragePath(name: String): StoragePath =
    new StoragePathImpl(name)

  private def initConfigProperty(name: String): ConfigProperty =
    new ConfigPropertyImpl(if (!name.isEmpty) name.substring(1) else "root")
}
