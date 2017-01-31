package com.napster.bi.sparkapp

import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

object AppName {

  val separator = "-"
  val dateFormat = DateTimeFormat.forPattern("yyyyMMdd")

  def dateRange(fromDate: LocalDate, toDate: LocalDate): String =
    if (fromDate.isEqual(toDate))
      dateFormat.print(fromDate)
    else
      s"${dateFormat.print(fromDate)}${separator}${dateFormat.print(toDate)}"

  def withRange(prefix: String, fromDate: LocalDate, toDate: LocalDate) =
    s"${prefix}${separator}${dateRange(fromDate, toDate)}"

  def withDate(prefix: String, date: LocalDate) =
    s"${prefix}${separator}${dateFormat.print(date)}"

}