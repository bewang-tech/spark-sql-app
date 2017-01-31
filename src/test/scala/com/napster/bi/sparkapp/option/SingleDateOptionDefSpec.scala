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

      def parse(args: String*) = parser.parse(args, CmdLineOption())

      implicit class ShouldDate(opt: Option[CmdLineOption]) {

        def should_date_be(expected: Option[LocalDate]) = {
          opt should not be (None)
          opt.get.date should be(expected)
        }

      }

      "return None" when {
        "-d or --date is not present" in {
          parse() should_date_be None
        }
      }
      "return the specified date" when {
        s"-d $testDate" in {
          parse("-d", testDate.toString) should_date_be Some(testDate)
        }
        s"--date $testDate" in {
          parse("--date", testDate.toString) should_date_be Some(testDate)
        }
      }
    }
  }

}
