package scopt

import org.scalatest.{Matchers, WordSpec}

class AppOptionParserSpec extends WordSpec with Matchers {

  trait Test {
    val defaultParser = new scopt.OptionParser[AppOption]("testapp") with AppOption.Parser

    val parser: AppOption.Parser

    def parse(args: String*)(validate: AppOption => Unit) =
      parser.parseCommandLine(args).map { appOption =>
        validate(appOption)
      } orElse {
        fail(s"failed to parse the arguments '${args.mkString(" ")}")
      }
  }

  "AppOption.Parser" should {
    "have a default option _app as the parser's programName" in {
      new Test {
        val parser = defaultParser

        parse() { appOption =>
          appOption._app.as[String] should be("testapp")
        }
      }
    }
    "have a default optional option: app-conf" when {
      "--app-conf is not present, is None" in {
        new Test {
          val parser = defaultParser

          parse() { appOption =>
            appOption.`app-conf`.asOption[String] should be(None)
          }
        }
      }
      "--app-conf test.conf, is None" in {
        new Test {
          val parser = defaultParser

          parse("--app-conf", "test.conf") { appOption =>
            appOption.`app-conf`.asOption[String] should be(Some("test.conf"))
          }
        }
      }

    }
    "allow to retrieve the option value" which {
      "is a global option" in {
        new Test {
          val parser = new scopt.OptionParser[AppOption]("testapp") with AppOption.Parser {
            opt[Int]('d', "data") valueName ("<int>") text "integer data"
          }

          parse("--data", "15") { appOption =>
            appOption.data.as[Int] should be(15)
          }
        }
      }
      "is a command option" in {
        new Test {
          val parser = new scopt.OptionParser[AppOption]("testapp") with AppOption.Parser {
            cmd("cmd_a")
              .text("do something about a")
              .children(
                opt[Int]('d', "data") valueName ("<int>") text "cmd_a integer data"
              )
          }

          parse("cmd_a", "--data", "15") { appOpt =>
            appOpt.cmd_a.data.as[Int] should be(15)
          }
        }
      }
    }
  }

}
