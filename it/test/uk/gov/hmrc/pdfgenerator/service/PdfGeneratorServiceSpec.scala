/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.pdfgenerator.service

import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.{Configuration, Environment, Mode}

import scala.util.Success



object PdfGeneratorServiceIntegrationFixture {
  def html : String = "<html><head></head><body><p>Hello</p></body></html>"
  def enableLinksFalse: Boolean = false
  def enableLinksTrue: Boolean = true
}

class PdfGeneratorServiceIntegrationSpec extends AnyWordSpec with Matchers with GuiceOneAppPerTest {


  val testConfig = new Configuration(ConfigFactory.load())
  val simple = Environment.simple()
  val environment = Environment.apply(simple.rootPath, simple.classLoader, Mode.Test)
  val pdfGeneratorService = new PdfGeneratorService(testConfig, ResourceHelper.apply, environment)

  "A PdfGeneratorService" should {
    "generate a pdf without links" in {
      val triedFile = pdfGeneratorService.generatePdf(PdfGeneratorServiceIntegrationFixture.html, PdfGeneratorServiceIntegrationFixture.enableLinksFalse)
      triedFile shouldBe a[Success[_]]
    }
    "generate a pdf with links" in {
      val triedFile = pdfGeneratorService.generatePdf(PdfGeneratorServiceIntegrationFixture.html, PdfGeneratorServiceIntegrationFixture.enableLinksTrue)
      triedFile shouldBe a[Success[_]]
    }
  }
}
