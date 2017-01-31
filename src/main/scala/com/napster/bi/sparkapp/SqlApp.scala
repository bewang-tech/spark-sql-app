package com.napster.bi.sparkapp

import com.napster.bi.config.AppConfig
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import scopt.AppOption

trait SqlApp extends LazyLogging {

  import SqlApp._

  def createDriver(appOpt: AppOption, sessionWithName: Option[String] => SparkSession): Driver

  def parse(args: Array[String]): Option[AppOption]

  def createSession(defaultAppName: Option[String] = None): SparkSession = {
    val sparkConf = new SparkConf()
    for (name <- sparkConf.getOption(SPARK_APP_NAME).orElse(defaultAppName)) {
      sparkConf.set(SPARK_APP_NAME, name)
    }

    val spark = SparkSession.builder()
      .config(sparkConf)
      .config("spark.sql.parquet.binaryAsString", "true")
      .enableHiveSupport()
      .getOrCreate()

    initSql(spark)

    spark
  }

  def initSql(spark: SparkSession): Unit = {
    spark.sql("set parquet.compression=gzip")
    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
  }

  def appConfig(appOption: AppOption) = {
    val config = appOption("app-conf").asOption[String]
      .map(confFile => ConfigFactory.load(confFile))
      .getOrElse(ConfigFactory.load())
    AppConfig(appOption._app, config)
  }

  def main(args: Array[String]) = {
    logger.info(s"The command line args: ${args.mkString(",")}")

    parse(args).map { appOpt =>
      logger.info(s"Application option = ${appOpt}")

      val driver = createDriver(appOpt, createSession _)

      logger.info(s"Running $driver ...")

      driver.run()

      logger.info(s"$driver finished successfully!")
    } getOrElse {
      throw new IllegalArgumentException(s"Cannot parse command line: ${args.mkString(",")}")
    }
  }

}

object SqlApp {

  val SPARK_APP_NAME = "spark.app.name"

}
