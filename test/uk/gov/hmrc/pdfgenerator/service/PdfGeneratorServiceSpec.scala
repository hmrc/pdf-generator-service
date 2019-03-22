package uk.gov.hmrc.pdfgenerator.service

import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, WordSpec, Matchers}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.pdfgenerator.resources._
import org.mockito.Mockito._

class PdfGeneratorServiceSpec extends WordSpec with MustMatchers{

  val testConfig = new Configuration(ConfigFactory.load("test-application.conf"))
  val service = new PdfGeneratorService(testConfig, MockResourceHelper)

  "A PdfGeneratorService" should {
    "load the default production config values" in {
      assert(service.ADOBE_COLOR_PROFILE_FULL_PATH == service.PROD_ROOT + "AdobeRGB1998.icc")
      assert(service.PS_DEF_FILE_FULL_PATH.endsWith("PDFA_def.ps"))
      assert(service.RUN_MODE == "prod")
      assert(service.GS_ALIAS.endsWith("/bin/gs-920-linux_x86_64"))
      assert(service.WK_TO_HTML_EXECUTABLE.endsWith("/bin/wkhtmltopdf"))

    }
  }

  val testHtmlDoc =
    <html>
      <body>
      </body>
    </html>.mkString

  val testHtmlDoc2 =
    <html>
      <body>
        <a href="https://www.matt.com"></a>
      </body>
    </html>.mkString

  val testHtmlDoc3 =
    <html>
      <body>
        <a href="https://www.matt.com"></a>
        <a href="https://www.matt.com"></a>
      </body>
    </html>.mkString

  "extractLinksFromHtml" should {
    "should return an empty list" in {
      val result = service.extractLinksFromHtml(testHtmlDoc)
      result.size mustBe 0
    }


    "should return a list of 1 links as strings" in {
      val result = service.extractLinksFromHtml(testHtmlDoc2)
      result.size mustBe 1
    }

    "should return a list of 2 links as strings" in {
      val result = service.extractLinksFromHtml(testHtmlDoc3)
      result.size mustBe 2
    }
  }
}
