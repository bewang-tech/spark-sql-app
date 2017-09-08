package com.napster.bi.sparkapp

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.spark.sql.SparkSession

trait Driver extends LazyLogging {

  def run(): Unit

}

object Driver {

  type Factory = SparkSession => Driver

  /**
    * Return a factory which creates a Driver running the specified function
    *
    * @param f the driver's run function.
    * @return the factory create the driver.
    */
  def run(f: SparkSession => Unit): Factory = { spark: SparkSession =>

    new Driver {
      override def run(): Unit = f(spark)
    }

  }

}