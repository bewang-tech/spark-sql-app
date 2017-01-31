package com.napster.bi.sparkapp

import org.joda.time.LocalDate
import org.scalatest.{Matchers, WordSpec}

class AppNameSpec extends WordSpec with Matchers {

  val prefix = "task-name"
  val testDate = new LocalDate("2017-01-01")

  "AppName" should {
    "create a name with a prefix and date" when {
      s"using withDate($prefix, $testDate)" in {
        AppName.withDate(prefix, testDate) should be(s"$prefix-20170101")
      }
      s"using the same fromDate and toDate in withRange($prefix, $testDate, $testDate)" in {
        AppName.withRange(prefix, testDate, testDate) should be(s"$prefix-20170101")
      }
    }
    "create a name with a prefix and a date range" in {
      AppName.withRange(prefix, testDate, testDate.plusDays(2)) should be(s"$prefix-20170101-20170103")
    }
  }

}
