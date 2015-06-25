/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
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

package com.stratio.sparkta.testat.operators

import scala.reflect.io.File

import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import com.stratio.sparkta.testat.SparktaATSuite

/**
 * Acceptance test:
 * [Input]: Socket.
 * [Output]: Parquet.
 * [Operators]: accumulator, avg, count, firsValue, fullText, lastValue, max,
 * median, min, range, stddev, sum, variance.
 */
@RunWith(classOf[JUnitRunner])
class ISocketOParquetOperatorsIT extends SparktaATSuite {

  override val PathToCsv = getClass.getClassLoader.getResource("fixtures/at-data-operators.csv").getPath
  override val policyFile = "policies/ISocket-OParquet-operators.json"
  val parquetPath = policyDto.outputs(0).configuration("path").toString
  val NumExecutors = 4
  val NumEventsExpected = 8

  "Sparkta" should {
    "starts and executes a policy that reads from a socket and writes in parquet" in {
      sparktaRunner
      checkData
    }


    def checkData(): Unit = {
      val sqc = new SQLContext(new SparkContext(s"local[$NumExecutors]", "ISocketOParquet-operatros"))
      val df = sqc.parquetFile(parquetPath).toDF
      // scalastyle:off magic.number
      val mapValues = df.map(row => Map(
        "product" -> row.getString(0),
        "acc_price" -> row.getSeq[String](2),
        "avg_price" -> row.getDouble(3),
        "count" -> row.getLong(4),
        "first_price" -> row.getString(5),
        "fulltext_price" -> row.getString(6),
        "last_price" -> row.getString(7),
        "max_price" -> row.getDouble(8),
        "median_price" -> row.getDouble(9),
        "min_price" -> row.getDouble(10),
        "range_price" -> row.getDouble(11),
        "stddev_price" -> row.getDouble(12),
        "sum_price" -> row.getDouble(13),
        "variance_price" -> row.getDouble(14)
      ))
      // scalastyle:on magic.number
      val productA = mapValues.filter(value => value("product") == "producta").take(1)(0)
      productA("acc_price") should be(
        Seq("10", "500", "1000", "500", "1000", "500", "1002", "600"))
      productA("avg_price") should be(639.0d)
      productA("sum_price") should be(5112.0d)
      productA("count") should be(NumEventsExpected)
      productA("first_price") should be("10")
      productA("last_price") should be("600")
      productA("max_price") should be(1002.0d)
      productA("min_price") should be(10.0d)
      productA("fulltext_price") should be("10 500 1000 500 1000 500 1002 600")
      productA("stddev_price") should be(347.9605889013459d)
      productA("variance_price") should be(121076.57142857143d)
      productA("range_price") should be(992.0d)
      val productB = mapValues.filter(value => value("product") == "productb").take(1)(0)
      productB("acc_price") should be(
        Seq("15", "1000", "1000", "1000", "1000", "1000", "1001", "50"))
      productB("avg_price") should be(758.25d)
      productB("sum_price") should be(6066.0d)
      productB("count") should be(NumEventsExpected)
      productB("first_price") should be("15")
      productB("last_price") should be("50")
      productB("max_price") should be(1001.0d)
      productB("min_price") should be(15.0d)
      productB("fulltext_price") should be("15 1000 1000 1000 1000 1000 1001 50")
      productB("stddev_price") should be(448.04041590655d)
      productB("variance_price") should be(200740.2142857143d)
      productB("range_price") should be(986.0d)
      sqc.sparkContext.stop
    }
  }

  override def extraAfter: Unit = File(parquetPath).deleteRecursively

  override def extraBefore: Unit = {}
}
