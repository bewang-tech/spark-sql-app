package com.napster.bi.sparkapp

import com.napster.bi.config.AppConfig
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.spark.sql.SparkSession

trait SqlApp[APPOPT <: WithConfFile] extends LazyLogging {

  def createDriver(appOpt: APPOPT)(implicit spark: SparkSession): Driver

  def parse(args: Array[String]): Option[APPOPT]

  def confRoot(opt: APPOPT): String

  def sparkSession =
    SparkSession.builder()
      .config("spark.sql.parquet.binaryAsString", "true")
      .enableHiveSupport()
      .getOrCreate()

  def initSql(spark: SparkSession): Unit = {
    spark.sql("set parquet.compression=gzip")
    spark.sql("set hive.exec.dynamic.partition.mode=nonstrict")
  }

  def loadConfig(confFile: Option[String]) =
    confFile match {
      case Some(f) => ConfigFactory.load(f)
      case None => ConfigFactory.load()
    }

  def appConfig(opt: APPOPT) = AppConfig(confRoot(opt), loadConfig(opt.confFile))

  def main(args: Array[String]) = {
    logger.info(s"The command line args: ${args.mkString(",")}")

    parse(args).map { opt =>
      logger.info(s"Application option = ${opt}")

      val spark = sparkSession
      initSql(spark)

      val driver = createDriver(opt)(spark)

      logger.info(s"Running $driver ...")

      driver.run()

      logger.info(s"$driver finished successfully!")
    } getOrElse {
      throw new IllegalArgumentException(s"Cannot parse command line: ${args.mkString(",")}")
    }
  }

}
