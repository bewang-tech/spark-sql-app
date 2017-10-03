package com.napster.bi.sparkapp

/**
  * Run a sequence of drivers in the order.
  *
  * @param drivers a seq of drivers
  */
class SeqDriver(drivers: Seq[Driver]) extends Driver {

  override def run(): Unit =
    drivers.foreach { driver =>
      logger.info(s"Running $driver ...")
      driver.run()
    }

  override def toString: String = s"SeqDriver(n=${drivers.size})"

}

object SeqDriver {

  def apply(drivers: Driver*): Driver = new SeqDriver(drivers)

}