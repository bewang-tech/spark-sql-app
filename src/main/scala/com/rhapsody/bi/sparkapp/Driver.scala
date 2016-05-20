package com.rhapsody.bi.sparkapp

import com.typesafe.scalalogging.slf4j.LazyLogging

trait Driver extends LazyLogging {

  def run(): Unit

}