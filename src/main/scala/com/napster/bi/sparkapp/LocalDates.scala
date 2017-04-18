package com.napster.bi.sparkapp

import org.joda.time.LocalDate

object LocalDates {

  lazy val today = LocalDate.now()
  lazy val yesterday = today.minusDays(1)

}
