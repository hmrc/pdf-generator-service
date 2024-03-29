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

package uk.gov.hmrc.pdfgenerator.service

import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.{Configuration, Environment}
import uk.gov.hmrc.pdfgenerator.resources._

class PdfGeneratorServiceSpec extends AnyWordSpec with Matchers {

  val testConfig = new Configuration(ConfigFactory.load("test-application.conf"))
  val service = new PdfGeneratorService(testConfig, MockResourceHelper, Environment.simple())

  "A PdfGeneratorService" should {
    "load the default production config values" in {
      assert(service.ADOBE_COLOR_PROFILE_FULL_PATH == service.PROD_ROOT + "AdobeRGB1998.icc")
      assert(service.PS_DEF_FILE_FULL_PATH.endsWith("PDFA_def.ps"))
      assert(!service.BASE_DIR_DEV_MODE)
      assert(service.GS_ALIAS.endsWith("/bin/gs-920-linux_x86_64"))
      assert(service.WK_TO_HTML_EXECUTABLE.endsWith("/bin/wkhtmltopdf"))

    }
  }

  val validLinksHtml =
    <html>
      <body>
        <a href="https://www.testa.gov.uk">test1</a>
        <a href="https://www.testb.gov.uk">test2</a>
        <a href="https://www.testc.gov.uk/characters">test3</a>
        <a href="https://www.testc.gov.uk/cha-rac-ters/25245234/">test4</a>
        <a href="https://www.qa.tax.service.gov.uk/two-way-message-adviser-frontend/message/123/reply">test5</a>
        <a href="http://localhost:1234/test">test6</a>
      </body>
    </html>.mkString

  val invalidLinksHtml1 =
    <html>
      <body>
        <a href="https://www.testa.com">test1</a>
      </body>
    </html>.mkString

  val invalidLinksHtml2 =
    <html>
      <body>
        <a href="http://www.testb.gov.uk.com">test2</a>
      </body>
    </html>.mkString

  "externalLinkEnabler" should {

    "return false when only valid links are inside the html" in {
      val result = service.getLinksDisabled(validLinksHtml)
      result mustBe false
    }

    "return true when any invalid link does not contain correct domain" in {
      val result = service.getLinksDisabled(invalidLinksHtml1)
      result mustBe true
    }

    "return true when any invalid link does end with the correct domain" in {
      val result = service.getLinksDisabled(invalidLinksHtml2)
      result mustBe true
    }
  }
}
