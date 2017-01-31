package scopt

import org.scalatest.{Matchers, WordSpec}

class AppOptionParserSpec extends WordSpec with Matchers {

  trait Test {
    val parser: OptionParser[AppOption]

    def parse(args: String*)(validate: AppOption.Node => Unit) =
      parser.parse(Array("--data", "15"), new AppOption()).map { c =>
        validate(AppOption.read(c))
      } orElse {
        fail(s"failed to parse the arguments '${args.mkString(" ")}")
      }
  }

  "AppOptionParser" should {
    "allow to retrieve the option value" which {
      "is a global option" in {
        new Test {
          val parser = new OptionParser[AppOption]("prog") with AppOption.Parser {
            opt[Int]('d', "data") valueName ("<int>") text "integer data"
          }

          parse("data", "15") { appOption =>
            appOption.data should be(Some(15))
          }
        }
      }
      "is a cmd option" in {
        new Test {
          val parser = new OptionParser[AppOption]("prog") with AppOption.Parser {
            cmd("cmd_a")
              .text("do something about a")
              .children(
                opt[Int]('d', "data") valueName ("<int>") text "integer data"
              )
          }
          parse("cmd_a", "--data", "15") { appOpt =>
            appOpt.cmd_a.data should be(Some(15))
          }
        }
      }
    }
  }

}
