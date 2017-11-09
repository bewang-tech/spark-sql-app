package com.napster.bi.sparkapp

import com.napster.bi.config.AppConfig
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.spark.sql.{DataFrame, SparkSession}
import scopt.AppOption

trait Driver extends LazyLogging {

  def printInfo(df: DataFrame): Unit = {
    println(df.schema.treeString)
    df.explain(true)
  }

  def run(): Unit

}

object Driver {

  trait Request

  trait RequestFactory {
    def apply(appOpt: AppOption, appConf: AppConfig): Request
  }

  trait Factory {
    def apply(request: Request)(implicit spark: SparkSession): Driver
  }

  val dummyDriver = new Driver {
    override def run(): Unit = {}
  }

}