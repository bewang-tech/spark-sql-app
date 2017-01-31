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

    override def toString = s"($path: $value)"

  }

  case class Container(path: String) extends AppOption {

    private[this] val _options = collection.mutable.Map[String, Any]()

    def as[T] =
      throw new UnsupportedOperationException(s"$path: ${c} is not a value.")

    def asOption[T] = as[T]

    def put(name: String, value: Any) =
      _options.put(name, value)

    override def apply(name: String): AppOption =
      _options.getOrElseUpdate(name, c.get(name).map { child =>
        val childPath = s"${path}.${name}"
        if (child.isInstanceOf[AppOption])
          Container(childPath, child.asInstanceOf[AppOption])
        else Value(childPath, child)
      } getOrElse {
        throw new NoSuchElementException(s"$name cannot be found at $path: ${c}")
      })
  }

  implicit def node2Int(nd: AppOption) = nd.as[Int]

  implicit def node2String(nd: AppOption) = nd.as[String]

  implicit def node2Boolean(nd: AppOption) = nd.as[Boolean]

  implicit def node2LocalDate(nd: AppOption) = nd.as[LocalDate]

  /**
    * This trait enables the child call `scopt.OptionParser#parse` using `super.parse`
    * when it overrides the method of `parse`. Otherwise, using `self.parse` leads to
    * an infinite loop.
    *
    * @tparam C
    */
  trait ParserOverridable[C] {
    def parse(args: Seq[String], conf: C): Option[C]
  }

  /**
    * This trait enable an app use AppOption to save the config from the command line
    * so that an app does not have to define its own config case class.
    */
  trait Parser extends ParserOverridable[AppOption] {
    self: scopt.OptionParser[AppOption] =>

    abstract override def parse(args: Seq[String], initConf: AppOption): Option[AppOption] = {
      val cache = collection.mutable.Map[Int, AppOption]()

      // Get the container AppOption of the given OptionDef resides.
      // The container is in the cache keyed by the parent id of the OptionDef.
      // If no parent, the option is in the top level, return initConf
      def containerOf(o: OptionDef[_, AppOption]):  =
      o.getParentId.flatMap(pid => cache.get(pid)).getOrElse(initConf)

      // default action: save the option value into AppOption
      def save(o: OptionDef[_, AppOption])(x: Any, in: AppOption) = {
        val c = containerOf(o)
        if (o.kind == Cmd) {
          // set the current command
          c.put("_cmd", o.name)

          // create a nested AppOption to store the options for the command
          val cmdConf = new AppOption()
          c.put(o.name, cmdConf)

          // cache the AppOption of the command
          cache.put(o.id, cmdConf)
        } else //
          c.put(o.name, x)

        // always initConf so that the updated initConf will be returned by `parser.parse(args, initConf)`
        initConf
      }

      // inject the action using the default function.
      options.foreach(o => o.action(save(o) _))

      super.parse(args, initConf)
    }

  }

}