package com.napster.bi.sparkapp.option

import org.joda.time.LocalDate
import org.scalatest.{Matchers, WordSpec}

class DateRangeOptionDefSpec extends WordSpec with Matchers {

  "DateOptionDef" should {
    "allow define an optional date range" when {
      case class CmdLineOption(fromDate: Option[LocalDate] = None,
                               toDate: Option[LocalDate] = None)

      val parser = new scopt.OptionParser[CmdLineOption]("test-app") with DateOptionDef[CmdLineOption] {
        range(inclusive(from), exclusive(to)) match {
          case Seq(f, t) =>
            (f action { (d, c) => c.copy(fromDate = Some(d)) },
              t action { (d, c) => c.copy(toDate = Some(d)) })
        }
      }

      val testDate1 = new LocalDate("2017-01-01")
      val testDate2 = new LocalDate("2017-01-02")

      def parse(args: String*)(validate: CmdLineOption => Unit) =
        parser.parse(args, CmdLineOption())
          .map { opt =>
            validate(opt)
            succeed
          }
          .getOrElse(fail(s"Parsing ${args.mkString(",")} failed"))

      "no --from-date and --to-date, both are None" in {
        parse() { opt =>
          opt.fromDate should be(None)
          opt.toDate should be(None)
        }
      }

      s"only --from-date $testDate1 is present, toDate is None" in {
        parse("--from-date", testDate1.toString()) { opt =>
          opt.fromDate should be(Some(testDate1))
          opt.toDate should be(None)
        }
      }

      s"only --to-date $testDate2 is present, fromDate is None" in {
        parse("--to-date", testDate2.toString()) { opt =>
          opt.fromDate should be(None)
          opt.toDate should be(Some(testDate2))
        }
      }

      s"--from-date $testDate1 --to-date $testDate2" in {
        parse(
          "--from-date", testDate1.toString,
          "--to-date", testDate2.toString()
        ) { opt =>
          opt.fromDate should be(Some(testDate1))
          opt.toDate should be(Some(testDate2))
        }
      }
    }
  }

}
