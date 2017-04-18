package com.napster.bi.sparkapp.option

import org.joda.time.LocalDate
import scopt.OptionDef

trait DateOptionDef[C] {
  self: scopt.OptionParser[C] =>

  import DateOptionDef._

  implicit val localDatesRead: scopt.Read[LocalDate] = scopt.Read.reads(s => new LocalDate(s))

  /**
    * Convenient method of create a date option
    *
    * @param short
    * @param name
    * @return
    */
  def date(name: String = "date",
           short: Option[Char] = Some('d'),
           desc: String = "the specified date.") =
    short.map(x => opt[LocalDate](x, name))
      .getOrElse(opt[LocalDate](name))
      .valueName("<yyyy-MM-dd>")
      .text(desc)

  type RangeOptionDef = (OptionDef[LocalDate, C], OptionDef[LocalDate, C])

  /**
    * Create a date range
    *
    * @param lower the option for the range's start date
    * @param upper the option for the range's end date
    * @return a tuple (fromOptionDef, toOptionDef)
    */
  def range(lower: RangeEnd, upper: RangeEnd) = {
    val open = symbols("lower")(lower.bound)
    val close = symbols("upper")(upper.bound)

    def desc(re: RangeEnd) = s"the ${re.end} date (${re.bound}) of range ${open}${lower.end},${upper.end}${close}"

    Seq(date(lower.name, lower.short, desc(lower)),
      date(upper.name, upper.short, desc(upper)))
  }

  def inclusive(rangeEnd: RangeEnd) = rangeEnd.copy(bound = Bound.inclusive)

  def exclusive(rangeEnd: RangeEnd) = rangeEnd.copy(bound = Bound.exclusive)

  val from = RangeEnd("from", "from-date")
  val to = RangeEnd("to", "to-date")
}

object DateOptionDef {

  object Bound extends Enumeration {
    type Bound = Value
    val inclusive, exclusive = Value
  }

  import Bound._

  val symbols = Map(
    "lower" -> Map(inclusive -> "[", exclusive -> "("),
    "upper" -> Map(inclusive -> "]", exclusive -> ")")
  )

  case class RangeEnd(end: String, // a string will be used in description
                      name: String, // the option name
                      short: Option[Char] = None, // the short name
                      bound: Bound = inclusive // inclusive or exclusive
                     )

}

