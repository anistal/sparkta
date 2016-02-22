/**
 * Copyright (C) 2016 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.sparkta.driver.factory

import java.io.File
import java.net.URI

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.serving.core.SparktaConfig
import com.stratio.sparkta.serving.core.constants.AppConstant
import com.typesafe.config.Config
import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming.{Duration, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConversions._
import scala.util.Try

object SparkContextFactory extends SLF4JLogging {

  private var sc: Option[SparkContext] = None
  private var sqlContext: Option[SQLContext] = None
  private var ssc: Option[StreamingContext] = None

  def sparkSqlContextInstance: Option[SQLContext] = {
    synchronized {
      sqlContext match {
        case Some(_) => sqlContext
        case None => if (sc.isDefined) sqlContext = Some(SQLContext.getOrCreate(sc.get))
      }
    }
    sqlContext
  }

  def sparkStreamingInstance: Option[StreamingContext] = ssc

  def sparkStreamingInstance(batchDuration: Duration, checkpointDir: String): Option[StreamingContext] = {
    synchronized {
      ssc match {
        case Some(_) => ssc
        case None => ssc = Some(getNewStreamingContext(batchDuration, checkpointDir))
      }
    }
    ssc
  }

  def setSparkContext(createdContext: SparkContext): Unit = sc = Option(createdContext)

  def setSparkStreamingContext(createdContext: StreamingContext): Unit = ssc = Option(createdContext)

  private def getNewStreamingContext(batchDuration: Duration, checkpointDir: String): StreamingContext = {
    val ssc = new StreamingContext(sc.get, batchDuration)
    ssc.checkpoint(checkpointDir)
    ssc
  }

  def sparkStandAloneContextInstance(generalConfig: Option[Config],
                                     specificConfig: Map[String, String],
                                     jars: Seq[File]): SparkContext =
    synchronized {
      sc.getOrElse(instantiateStandAloneContext(generalConfig, specificConfig, jars))
    }

  def sparkClusterContextInstance(specificConfig: Map[String, String]): SparkContext =
    synchronized {
      sc.getOrElse(instantiateClusterContext(specificConfig))
    }

  private def instantiateStandAloneContext(generalConfig: Option[Config],
                                           specificConfig: Map[String, String],
                                           jars: Seq[File]): SparkContext = {
    sc = Some(SparkContext.getOrCreate(configToSparkConf(generalConfig, specificConfig)))
    jars.foreach(f => sc.get.addJar(f.getAbsolutePath))
    sc.get
  }

  private def instantiateClusterContext(specificConfig: Map[String, String]): SparkContext = {
    sc = Some(SparkContext.getOrCreate(configToSparkConf(None, specificConfig)))
    sc.get
  }

  private def configToSparkConf(generalConfig: Option[Config], specificConfig: Map[String, String]): SparkConf = {
    val conf = new SparkConf()
    if (generalConfig.isDefined) {
      val properties = generalConfig.get.entrySet()
      properties.foreach(e => {
        if (e.getKey.startsWith("spark."))
          conf.set(e.getKey, generalConfig.get.getString(e.getKey))
      })
    }
    specificConfig.foreach { case (key, value) => conf.set(key, value) }

    conf
  }

  def destroySparkStreamingContext(): Unit = {
    synchronized {
      ssc.fold(log.warn("Spark Streaming Context is empty")) { streamingContext =>
        try {
          val stopGracefully = Try(SparktaConfig.getDetailConfig.get.getBoolean(AppConstant.ConfigStopGracefully))
            .getOrElse(true)
          val stopTimeout = Try(SparktaConfig.getDetailConfig.get.getInt(AppConstant.StopTimeout))
            .getOrElse(AppConstant.DefaultStopTimeout)
          log.info(s"Stopping streamingContext with name: ${streamingContext.sparkContext.appName}")
          Try(streamingContext.stop(false, stopGracefully)).getOrElse(streamingContext.stop(false, false))
          if (streamingContext.awaitTerminationOrTimeout(stopTimeout))
            log.info(s"Stopped streamingContext with name: ${streamingContext.sparkContext.appName}")
          else log.info(s"StreamingContext with name: ${streamingContext.sparkContext.appName} not Stopped: timeout")
        } finally {
          ssc = None
        }
      }
    }
  }

  def destroySparkContext(): Unit = {
    synchronized {
      destroySparkStreamingContext()
      sc.fold(log.warn("Spark Context is empty")) { sparkContext =>
        try {
          log.info("Stopping SparkContext with name: " + sparkContext.appName)
          sparkContext.stop()
          log.info("Stopped SparkContext with name: " + sparkContext.appName)
        } finally {
          sc = None
        }
      }
    }
  }
}
