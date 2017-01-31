package com.napster.bi.sparkapp

import com.typesafe.config.Config
import org.apache.spark.sql._

trait BoundCatalog {

  def tableName(alias: String): String

  def readTable(alias: String): DataFrame

}

class ConfigBoundCatalog(config: Config)(implicit spark: SparkSession) extends BoundCatalog {

  override def tableName(alias: String) = config.getString(alias)

  override def readTable(alias: String) = spark.read.table(tableName(alias))

}

class MapBoundCatalog(tableMap: Map[String, String])(implicit spark: SparkSession) extends BoundCatalog {

  override def tableName(alias: String) = tableMap(alias)

  override def readTable(alias: String) = spark.read.table(tableName(alias))

}

object BoundCatalog {

  def apply(config: Config)(implicit spark: SparkSession) =
    new ConfigBoundCatalog(config)

  def apply(tableMap: Map[String, String])(implicit spark: SparkSession) =
    new MapBoundCatalog(tableMap)

}