package com.rhapsody.bi.sparkapp

class CompositeDriver(drivers: Seq[Driver]) extends Driver {

  override def run(): Unit = drivers.foreach { driver =>
    logger.info(s"Running $driver ...")
    driver.run()
  }

}

object CompositeDriver {

  def apply(drivers: Driver*) = new CompositeDriver(drivers)

}
