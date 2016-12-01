package com.napster.bi.sparkapp

import com.typesafe.config.Config
import org.apache.spark.sql._

trait BoundCatalog {

  def table(name: String): String

  def read(name: String): DataFrame

}

class ConfigBoundCatalog(config: Config)(implicit spark: SparkSession) extends BoundCatalog {

  override def table(name: String) = config.getString(name)

  override def read(name: String) = spark.read.table(table(name))

}

class MapBoundCatalog(tableMap: Map[String, String])(implicit spark: SparkSession) extends BoundCatalog {

  override def table(name: String) = tableMap(name)

  override def read(name: String) = spark.read.table(table(name))

}

object BoundCatalog {

  def apply(config: Config)(implicit spark: SparkSession) =
    new ConfigBoundCatalog(config)

  def apply(tableMap: Map[String, String])(implicit spark: SparkSession) =
    new MapBoundCatalog(tableMap)

}