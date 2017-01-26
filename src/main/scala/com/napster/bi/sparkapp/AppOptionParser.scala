package com.napster.bi.sparkapp

import org.joda.time.LocalDate

trait AppOptionParser[C] {
  self: scopt.OptionParser[C] =>

  type SetOption[A] = (A, C) => C

  implicit val localDatesRead: scopt.Read[LocalDate] = scopt.Read.reads(s => new LocalDate(s))

  def appConfig(setConfFile: SetOption[String]) =
    opt[String]("app-conf")
      .valueName("config_file_name")
      .action(setConfFile)
      .text("The config file must be in the class path. If this is not specified, application.conf is used.")

  def fromDate(setFromDate: SetOption[LocalDate]) =
    opt[LocalDate]("from-date")
      .valueName("yyyy-MM-dd")
      .action(setFromDate)
      .text("the start date of a date range [from, to] (inclusive).")

  def toDate(setToDate: SetOption[LocalDate]) =
    opt[LocalDate]("to-date")
      .valueName("yyyy-MM-dd")
      .action(setToDate)
      .text("the end date of a date range [from, to] (inclusive).")

  def date(setDate: SetOption[LocalDate]) =
    opt[LocalDate]('d', "date")
      .valueName("yyyy-MM-dd")
      .action(setDate)
      .text("the specifid date.")

  help("help").text("print this usage text")

}
