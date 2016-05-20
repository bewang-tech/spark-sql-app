package com.rhapsody.bi.sparkapp

import com.rhapsody.bi.config.AppConfig
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.slf4j.LazyLogging
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContextEx
import org.apache.spark.{SparkConf, SparkContext}

trait SqlApp[APPOPT <: WithConfFile] extends LazyLogging {

  def createDriver(sqlContext: SQLContext, appOpt: APPOPT): Driver

  def parse(args: Array[String]): Option[APPOPT]

  def confRoot: String

  def createSqlContext = {
    val conf = new SparkConf()
    val sc = new SparkContext(conf)

    val sqlContext = new HiveContextEx(sc)
    sqlContext.setConf("spark.sql.parquet.binaryAsString", "true")

    sqlContext
  }

  def initSql(sqlContext: SQLContext) = {
    sqlContext.sql("set parquet.compression=gzip")
    sqlContext.sql("set hive.exec.dynamic.partition.mode=nonstrict")
  }

  def loadConf(confFile: Option[String]) =
    confFile match {
      case Some(f) => ConfigFactory.load(f)
      case None => ConfigFactory.load()
    }

  def appConf(opt: APPOPT) = AppConfig(confRoot, loadConf(opt.confFile))

  def main(args: Array[String]) = {
    logger.info(s"The command line args: ${args.mkString(",")}")

    parse(args).map { opt =>
      logger.info(s"Application option = ${opt}")

      val sqlContext = createSqlContext
      initSql(sqlContext)

      val driver = createDriver(sqlContext, opt)

      logger.info(s"Running $driver ...")

      driver.run()

      logger.info(s"$driver finished successfully!")
    } getOrElse {
      throw new IllegalArgumentException(s"Cannot parse command line: ${args.mkString(",")}")
    }
  }

}
