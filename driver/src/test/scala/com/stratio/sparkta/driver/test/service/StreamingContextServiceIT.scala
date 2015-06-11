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

package com.stratio.sparkta.driver.test.service

import java.io.File

import akka.event.slf4j.SLF4JLogging
import com.stratio.sparkta.driver.constants.AppConstant
import com.stratio.sparkta.driver.dto.AggregationPoliciesDto
import com.stratio.sparkta.driver.helpers.sparkta.SparktaHelper
import com.stratio.sparkta.driver.service.StreamingContextService
import com.stratio.sparkta.sdk.JsoneyStringSerializer
import org.json4s.{DefaultFormats, native}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, WordSpecLike}
import spray.testkit.ScalatestRouteTest

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class StreamingContextServiceIT extends WordSpecLike
with ScalatestRouteTest
with Matchers
with SLF4JLogging {

  val PathToPolicy = "examples/policies/IKafka-OPrint.json"

  "A StreamingContextService should" should {
    "create spark streaming context from a policy" in {
      val sparktaConfig = SparktaHelper.initConfig("sparkta")
      val sparktaHome = SparktaHelper.initSparktaHome()
      val jars = SparktaHelper.initJars(AppConstant.JarPaths, sparktaHome)

      val streamingContextService = new StreamingContextService(sparktaConfig, jars)
      val json = Source.fromFile(new File(sparktaHome, PathToPolicy)).mkString
      implicit val formats = DefaultFormats + new JsoneyStringSerializer()
      val apConfig = native.Serialization.read[AggregationPoliciesDto](json)

      val ssc = streamingContextService.createStreamingContext(apConfig)

      ssc should not be None
    }
  }
}
