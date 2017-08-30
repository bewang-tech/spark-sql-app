package com.napster.bi.sparkapp.option

import org.joda.time.LocalDate
import org.scalatest.{Matchers, WordSpec}

class SingleDateOptionDefSpec extends WordSpec with Matchers {

  "DateOptionDef" should {

    "allow the app has an optional date as parameter" when {
      case class CmdLineOption(date: Option[LocalDate] = None)

      val parser = new scopt.OptionParser[CmdLineOption]("test-app") with DateOptionDef[CmdLineOption] {
        date() action { (d, c) => c.copy(date = Some(d)) }
      }

      val testDate = new LocalDate("2017-01-01")

      def parse(args: String*)(validate: CmdLineOption => Unit) =
        parser.parse(args, CmdLineOption())
          .map { opt =>
            validate(opt)
            succeed
          }
          .getOrElse(fail(s"Parsing ${args.mkString(",")} failed."))

      "return None" when {
        "-d or --date is not present" in {
          parse() { opt =>
            opt.date should be(None)
          }
        }
      }
      "return the specified date" when {
        s"-d $testDate" in {
          parse("-d", testDate.toString) { opt =>
            opt.date should be(Some(testDate))
          }
        }
        s"--date $testDate" in {
          parse("--date", testDate.toString) { opt =>
            opt.date should be(Some(testDate))
          }
        }
      }
    }
  }

}
