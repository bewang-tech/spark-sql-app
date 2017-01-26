package com.napster.bi.sparkapp

trait AppOption {

  /**
    * Spark will create a random name if the class doesn't provide and `spark.app.name` is not specified in `spark-submit`
    */
  def appName: Option[String] = None

  def confFile: Option[String]

}
