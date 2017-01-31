package scopt

import org.joda.time.LocalDate

import scala.language.dynamics

trait AppOption extends Dynamic {
  def path: String

  def apply(name: String): AppOption

  def as[T]: T

  def asOption[T]: Option[T]

  def selectDynamic(name: String): AppOption = apply(name)
}


object AppOption {

  case class Value(path: String, value: Any) extends AppOption {

    def as[T] = value.asInstanceOf[T]

    def asOption[T] = Some(as[T])

    override def apply(name: String): AppOption =
      throw new UnsupportedOperationException(s"${path} is a value.")

    override def toString = value.toString

  }

  case class NonExistValue(path: String) extends AppOption {
    def as[T] =
      throw new NoSuchElementException(s"$path doesn't exist")

    def asOption[T] = None

    override def apply(name: String): AppOption =
      throw new NoSuchElementException(s"$path doesn't exist")

    override def toString = "None"
  }

  case class Container(path: String) extends AppOption {

    private[this] val _options = collection.mutable.Map[String, AppOption]()

    def as[T] =
      throw new UnsupportedOperationException(s"$path is not a value.")

    def asOption[T] = as[T]

    def put(name: String, value: AppOption) =
      _options.put(name, value)

    override def apply(name: String): AppOption =
      _options.getOrElse(name, NonExistValue(s"${path}.${name}"))

    override def toString = _options.toString

  }

  implicit def node2Int(nd: AppOption) = nd.as[Int]

  implicit def node2String(nd: AppOption) = nd.as[String]

  implicit def node2Boolean(nd: AppOption) = nd.as[Boolean]

  implicit def node2LocalDate(nd: AppOption) = nd.as[LocalDate]

  /**
    * This trait enable an app use AppOption to save the config from the command line
    * so that an app does not have to define its own config case class.
    */
  trait Parser {
    self: scopt.OptionParser[AppOption] =>

    opt[String]("app-conf")
      .valueName("<config-file-name>")
      .text("The config file must be in the class path. If this is not specified, application.conf is used.")

    help("help").text("print this usage")

    // abstract override def parse(args: Seq[String], initConf: AppOption): Option[AppOption] = {
    def parseCommandLine(args: Seq[String]): Option[AppOption] = {
      val initConf = Container("$")
      initConf.put("_app", Value("$._app", self.programName))

      val cache = collection.mutable.Map[Int, AppOption]()

      // Get the container AppOption of the given OptionDef resides.
      // The container is in the cache keyed by the parent id of the OptionDef.
      // If no parent, the option is in the top level, return initConf
      def containerOf(o: OptionDef[_, AppOption]): Container = {
        o.getParentId.flatMap(pid => cache.get(pid))
          .map(_.asInstanceOf[Container]).getOrElse(initConf)
      }

      // default action: save the option value into AppOption
      def save(o: OptionDef[_, AppOption])(x: Any, in: AppOption) = {
        val c = containerOf(o)
        val childPath = s"${c.path}.${o.name}"
        if (o.kind == Cmd) {
          // set the selected command
          c.put("_cmd", Value(s"${c.path}._cmd", o.name))

          // create a nested AppOption to store the options for the command
          val cmdConf = Container(childPath)
          c.put(o.name, cmdConf)

          // cache the AppOption of the command
          cache.put(o.id, cmdConf)
        } else {
          c.put(o.name, Value(childPath, x))
        }

        // always initConf so that the updated initConf will be returned by `parser.parse(args, initConf)`
        initConf
      }

      // inject the action using the default function.
      options.foreach(o => o.action(save(o) _))

      self.parse(args, initConf)
    }

  }

}