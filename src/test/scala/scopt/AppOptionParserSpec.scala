package scopt

import org.scalatest.{Matchers, WordSpec}

class AppOptionParserSpec extends WordSpec with Matchers {

  trait Test {
    val defaultParser = new scopt.OptionParser[AppOption]("testapp") with AppOption.Parser

    val parser: AppOption.Parser

    def appOptionOf(args: String*) = parser.parseCommandLine(args)

    def parse(args: String*)(validate: AppOption => Unit) =
      appOptionOf(args: _*).map { appOption =>
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
    "allow a command's checkConfig using relative AppOption to the command" when {
      trait FailureTest extends Test {
        val parser = new scopt.OptionParser[AppOption]("testapp") with AppOption.Parser {
          cmd("cmd_c")
            .text("do something about c")
            .children(
              opt[Int]("lower") valueName ("<int>") text "lower bound",
              opt[Int]("upper") valueName ("<int>") text "upper bound")
            .children(
              checkConfig { cmdOpt =>
                def lowerLessThanUpper(lower: Int, upper: Int) =
                  if (lower < upper) success else failure(s"$lower is not less then $upper")

                lowerLessThanUpper(cmdOpt.lower, cmdOpt.upper)
              }
            )

          cmd("cmd_d")

          checkConfig(mustHaveCommand)
        }
      }
      "given arguments fail the check, should fail the parsing" in {
        new FailureTest {
          appOptionOf("cmd_c", "--lower", "100", "--upper", "1") should be(None)
        }
      }
      "the required args are not provided" in {
        new FailureTest {
          appOptionOf("cmd_a", "--data", "15") should be(None)
        }
      }
    }
  }

}
