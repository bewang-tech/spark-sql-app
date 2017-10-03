package com.napster.bi.sparkapp

import com.typesafe.config.Config
import org.apache.spark.sql._

trait BoundCatalog {

  def tableName(alias: String): String

  def readTable(alias: String): DataFrame

}

class ConfigBoundCatalog(config: Config)(implicit spark: SparkSession) extends BoundCatalog {

  override def tableName(alias: String): String =
    if (config.hasPath(alias)) config.getString(alias) else alias

  override def readTable(alias: String): DataFrame =
    spark.read.table(tableName(alias))

}

class MapBoundCatalog(tableMap: Map[String, String])(implicit spark: SparkSession) extends BoundCatalog {

  override def tableName(alias: String): String =
    tableMap.getOrElse(alias, alias)

  override def readTable(alias: String): DataFrame =
    spark.read.table(tableName(alias))

}

object BoundCatalog {

  def apply(config: Config)(implicit spark: SparkSession) =
    new ConfigBoundCatalog(config)

  def apply(tableMap: Map[String, String])(implicit spark: SparkSession) =
    new MapBoundCatalog(tableMap)

}