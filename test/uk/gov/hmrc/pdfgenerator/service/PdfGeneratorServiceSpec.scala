package uk.gov.hmrc.pdfgenerator.service

import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, WordSpec}
import play.api.Configuration
import uk.gov.hmrc.pdfgenerator.resources._

class PdfGeneratorServiceSpec extends WordSpec with MustMatchers{

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

  val testHtmlDoc1 =
    <html>
      <body>
        <a href="https://www.testa.gov.uk"></a>
        <a href="https://www.testb.gov.uk"></a>
        <a href="https://www.testc.gov.uk/characters"></a>
        <a href="https://www.testc.gov.uk/cha-rac-ters/25245234/"></a>
      </body>
    </html>.mkString


  val testHtmlDoc2 =
    <html>
      <body>
        <a href="https://www.testa.com"></a>
        <a href="http://www.testb.gov.uk"></a>
        <a href="https://www.testc.gov.uk"></a>
      </body>
    </html>.mkString

  "externalLinkEnabler" should {

    "return true when only valid links are inside the html" in {
      val result = service.externalLinkEnabler(testHtmlDoc1)
      result mustBe true
    }

    "return false when any invalid links are inside the html" in {
      val result = service.externalLinkEnabler(testHtmlDoc2)
      result mustBe false
    }
  }
}
