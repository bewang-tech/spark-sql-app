package scopt

import org.joda.time.LocalDate

import scala.language.{dynamics, implicitConversions}

trait AppOption extends Dynamic {
  def path: String

  def apply(name: String): AppOption

  def as[T]: T

  def getOrElse[T](default: T): T = asOption[T].getOrElse(default)

  def asOption[T]: Option[T]

  def exists: Boolean = true

  def selectDynamic(name: String): AppOption = apply(name)
}


object AppOption {

  case class Value(path: String, value: Any) extends AppOption {

    override def as[T]: T = value.asInstanceOf[T]

    override def asOption[T]: Option[T] = Some(as[T])

    override def apply(name: String): AppOption =
      throw new UnsupportedOperationException(s"$path is a value.")

    override def toString: String = value.toString

  }

  case class NonExistValue(path: String) extends AppOption {
    override def as[T]: T =
      throw new NoSuchElementException(s"$path doesn't exist")

    override def asOption[T] = None

    override def exists = false

    override def apply(name: String): AppOption =
      throw new NoSuchElementException(s"$path doesn't exist")

    override def toString = "None"
  }

  case class Container(path: String) extends AppOption {

    private[this] val _options = collection.mutable.Map[String, AppOption]()

    override def as[T]: T =
      throw new UnsupportedOperationException(s"$path is not a value.")

    override def asOption[T]: Option[T] =
      throw new UnsupportedOperationException(s"$path is not a value.")

    def put(name: String, value: AppOption): Option[AppOption] =
      _options.put(name, value)

    override def apply(name: String): AppOption =
      _options.getOrElse(name, NonExistValue(s"$path.$name"))

    override def toString: String = _options.toString

  }

  implicit def node2Int(nd: AppOption):Int = nd.as[Int]

  implicit def node2String(nd: AppOption): String = nd.as[String]

  implicit def node2Boolean(nd: AppOption): Boolean = nd.as[Boolean]

  implicit def node2LocalDate(nd: AppOption): LocalDate = nd.as[LocalDate]

  /**
    * This trait enable an app use AppOption to save the config from the command line
    * so that an app does not have to define its own config case class.
    */
  abstract class Parser(programName: String) extends scopt.OptionParser[AppOption](programName) {

    opt[String]("app-conf")
      .valueName("<config-file-name>")
      .text("The config file must be in the class path. If this is not specified, application.conf is used.")

    help("help").text("print this usage")

    private[this] val cache = collection.mutable.Map[Int, AppOption]()

    protected def pathOf(id: Int): String =
      options.find(_.id == id).map { o =>
        val parentPath = o.getParentId.map(parentId => pathOf(parentId)).getOrElse("$")
        s"$parentPath.${o.name}"
      }.getOrElse(s"non_exist_option($id)")

    protected def addScopedCheck(check: OptionDef[Unit, AppOption]): Unit = {
      val parentId = check.getParentId.get
      val path = pathOf(parentId)
      val checkOfScope = check.copy(_configValidations = Seq())

      def withScope(f: AppOption => Either[String, Unit])(appOption: AppOption) =
        cache.get(parentId) map { scope =>
          f(scope) match {
            case Right(_) => success
            case Left(msg) => failure(s"$path check failed: $msg")
          }
        } getOrElse success

      check.checks.foreach { f =>
        checkOfScope.validateConfig(withScope(f))
      }
    }

    // default action: save the option value into AppOption
    protected def save(o: OptionDef[_, AppOption])(x: Any, root: AppOption): AppOption = {
      val c = containerOf(o).getOrElse(root).asInstanceOf[Container]
      val childPath = s"${c.path}.${o.name}"
      if (o.kind == Cmd) {
        // set the selected command
        c.put("_cmd", Value(s"${c.path}._cmd", o.name))

        // create a nested AppOption to store the options for the command
        val cmdConf = Container(childPath)
        c.put(o.name, cmdConf)

        // cache the AppOption of the command
        cache.put(o.id, cmdConf)
      } else if (o.kind == Opt) {
        c.put(o.name, Value(childPath, x))
      }

      // always initConf so that the updated initConf will be returned by `parser.parse(args, initConf)`
      root
    }

    /** Get the container AppOption of the given OptionDef resides.
      * The container is in the cache keyed by the parent id of the OptionDef.
      * If no parent, the option is in the top level, return initConf
      * */
    protected def containerOf(o: OptionDef[_, AppOption]): Option[AppOption] =
      o.getParentId.flatMap(pid => cache.get(pid))

    protected def mustHaveCommand(appOption: AppOption): Either[String, Unit] =
      if (appOption._cmd.exists) success else failure("No command is specified.")

    private[this] var _initialized = false

    protected def initialize(): Unit = {
      // inject the action using the default function.
      options.filter(o => o.kind == Opt || o.kind == Cmd)
        .foreach(o => o.action(save(o)))

      // replace a check's validatioin function using its parent's AppOption
      checks.filter(_.hasParent)
        .map(_.asInstanceOf[OptionDef[Unit, AppOption]])
        .foreach(addScopedCheck)

      _initialized = true
    }

    // abstract override def parse(args: Seq[String], initConf: AppOption): Option[AppOption] = {
    def parseCommandLine(args: Seq[String]): Option[AppOption] = {
      if (!_initialized) initialize()

      val initConf = Container("$")
      initConf.put("_app", Value("$._app", programName))

      parse(args, initConf)
    }

  }

}