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

package com.stratio.sparkta.serving.api.ssl

import javax.net.ssl.SSLContext

import com.stratio.sparkta.serving.core.SparktaConfig
import org.apache.camel.util.jsse.{KeyManagersParameters, KeyStoreParameters, SSLContextParameters}
import spray.io._

trait SSLSupport {

  // if there is no SSLContext in scope implicitly the HttpServer uses the default SSLContext,
  // since we want non-default settings in this example we make a custom SSLContext available here
  implicit def sslContext: SSLContext = {
    val keyStoreFile = SparktaConfig.apiConfig.get.getString("certificate-file")

    val ksp = new KeyStoreParameters()
    ksp.setResource(keyStoreFile);
    ksp.setPassword(SparktaConfig.apiConfig.get.getString("certificate-password"))

    val kmp = new KeyManagersParameters()
    kmp.setKeyStore(ksp)
    kmp.setKeyPassword(SparktaConfig.apiConfig.get.getString("certificate-password"))

    val scp = new SSLContextParameters()
    scp.setKeyManagers(kmp)

    val context= scp.createSSLContext()

    context
  }

  // if there is no ServerSSLEngineProvider in scope implicitly the HttpServer uses the default one,
  // since we want to explicitly enable cipher suites and protocols we make a custom ServerSSLEngineProvider
  // available here
  implicit def sslEngineProvider: ServerSSLEngineProvider = {
    ServerSSLEngineProvider { engine =>
      engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
      engine.setEnabledProtocols(Array("SSLv3", "TLSv1"))
      engine
    }
  }
}
