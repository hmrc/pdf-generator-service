/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.pdfgenerator

import com.typesafe.config.ConfigFactory
import play.api.Configuration
import uk.gov.hmrc.pdfgenerator.service.ResourceHelper

/**
  * Created by peter on 27/03/2017.
  */
package object resources {

  val configuration = new Configuration(ConfigFactory.load("application.conf"))

  object MockResourceHelper extends ResourceHelper {
    override def setupExecutableSupportFiles(
      psDefFileBare: String,
      psDefFileFullPath: String,
      baserDir: String,
      colorProfileBare: String,
      colorProfileFullPath: String): Unit = {}
  }
}
