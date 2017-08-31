package com.napster.bi.sparkapp

import com.napster.bi.config.AppConfig
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.spark.sql.SparkSession
import scopt.AppOption

trait SqlApp extends LazyLogging {

  import SqlApp._

  def driverFactory: DriverFactory

  def createDriver(appOption: AppOption): Driver = {
    val spark = sparkSession

    initSql(spark)

    val appConf = appConfig(appOption)

    driverFactory.create(appOption, appConf)(spark)
  }

  def parse(args: Array[String]): Option[AppOption]

  def sparkSession: SparkSession =
    SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", "true")
      .enableHiveSupport()
      .getOrCreate()

  def initSql(spark: SparkSession): Unit = {
    spark.sql("set parquet.compression=gzip")
    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
  }

  def appConfig(appOption: AppOption): AppConfig = {
    val config = appOption("app-conf").asOption[String]
      .map(confFile => ConfigFactory.load(confFile))
      .getOrElse(ConfigFactory.load())
    AppConfig(appOption._app, config)
  }

  def run(appOpt: AppOption): Unit = {
    logger.info(s"Application option = $appOpt")

    val driver = createDriver(appOpt)

    logger.info(s"Running $driver ...")

    driver.run()

    logger.info(s"$driver finished successfully!")
  }

  def main(args: Array[String]): Unit = {
    logger.info(s"The command line args: ${args.mkString(",")}")

    parse(args) match {
      case Some(appOpt) =>
        run(appOpt)
      case None =>
        throw new IllegalArgumentException(s"Cannot parse command line: ${args.mkString(",")}")
    }
  }

}

object SqlApp {

  trait DriverFactory {
    def create(opt: AppOption, conf: AppConfig)(implicit spark: SparkSession): Driver
  }

}
