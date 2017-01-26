package com.napster.bi.sparkapp

import org.joda.time.LocalDate
import org.scalatest.{Matchers, WordSpec}

class AppOptionParserDateRangeSpec extends WordSpec with Matchers {

  "AppOption" should {
    "allow define an optional date range" when {
      case class CmdLineOption(fromDate: Option[LocalDate] = None,
                               toDate: Option[LocalDate] = None)

      val parser = new scopt.OptionParser[CmdLineOption]("test-app") with AppOptionParser[CmdLineOption] {
        fromDate { (d, c) => c.copy(fromDate = Some(d)) }
        toDate { (d, c) => c.copy(toDate = Some(d)) }
      }

      val testDate1 = new LocalDate("2017-01-01")
      val testDate2 = new LocalDate("2017-01-02")

      def parse(args: String*) = parser.parse(args, CmdLineOption())

      "no --from-date and --to-date, both are None" in {
        val opt = parse()
        opt should not be (None)
        opt.get.fromDate should be(None)
        opt.get.toDate should be(None)
      }

      s"only --from-date $testDate1 is present, toDate is None" in {
        val opt = parse("--from-date", testDate1.toString())
        opt should not be (None)
        opt.get.fromDate should be(Some(testDate1))
        opt.get.toDate should be(None)
      }

      s"only --to-date $testDate2 is present, fromDate is None" in {
        val opt = parse("--to-date", testDate2.toString())
        opt should not be (None)
        opt.get.fromDate should be(None)
        opt.get.toDate should be(Some(testDate2))
      }

      s"--from-date $testDate1 --to-date $testDate2" in {
        val opt = parse("--from-date", testDate1.toString, "--to-date", testDate2.toString())
        opt should not be (None)
        opt.get.fromDate should be(Some(testDate1))
        opt.get.toDate should be(Some(testDate2))
      }
    }
  }

}
