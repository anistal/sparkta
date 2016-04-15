/**
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stratio.benchmark.generator.models

case class SecuritasModel(sessionid: String,
                            family: String,
                            model: String,
                            version: String,
                            command: String,
                            phaseid: String,
                            timestamp: Long,
                            panelid: String,
                            country: String,
                            coverage: String,
                            coveragecategory: String,
                            coveragetype: String,
                            carrier: String,
                            lat: Long,
                            lng: Long,
                            content: String,
                            coveragemandatory: String,
                            texto: String,
                            codpostal: String,
                            year: String,
                            month: String,
                            day: String,
                            hour: String)