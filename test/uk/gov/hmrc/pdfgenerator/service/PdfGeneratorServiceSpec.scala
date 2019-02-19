package uk.gov.hmrc.pdfgenerator.service

import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, WordSpec}
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.pdfgenerator.resources._


class PdfGeneratorServiceSpec extends WordSpec with MustMatchers{

  val testConfig = new Configuration(ConfigFactory.load("test-application.conf"))
  val simple = Environment.simple()
  val environment = Environment.apply(simple.rootPath, simple.classLoader, Mode.Dev)
  val service = new PdfGeneratorService(testConfig, MockResourceHelper, environment)

  "A PdfGeneratorService" should {
    "load the default production config values" in {
      assert(service.ADOBE_COLOR_PROFILE_FULL_PATH == service.PROD_ROOT + "AdobeRGB1998.icc")
      assert(service.PS_DEF_FILE_FULL_PATH == service.PROD_ROOT + "PDFA_def.ps")
      assert(!service.BASE_DIR_DEV_MODE)
      assert(service.GS_ALIAS.endsWith("/bin/gs-920-linux_x86_64"))
      assert(service.WK_TO_HTML_EXECUABLE.endsWith("/bin/wkhtmltopdf"))

    }
  }


}
