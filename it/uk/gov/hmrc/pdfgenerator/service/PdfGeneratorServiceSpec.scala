package uk.gov.hmrc.pdfgenerator.service

import java.io.File

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import org.scalatest.{MustMatchers, WordSpec}
import play.api.Configuration
import uk.gov.hmrc.play.test.WithFakeApplication

import scala.util.Success



object PdfGeneratorServiceIntegrationFixture {
  def html : String = "<html><head></head><body><p>Hello</p></body></html>"
}

class PdfGeneratorServiceIntegrationSpec extends WordSpec with MustMatchers with WithFakeApplication{


  val testConfig = new Configuration(ConfigFactory.load().withValue("pdfGeneratorService.runmode", ConfigValueFactory.fromAnyRef("test")))
  val pdfGeneratorService = new PdfGeneratorService(testConfig, ResourceHelper.apply)

  "A PdfGeneratorService" should {
    "generate a pdf" in {
      assert(pdfGeneratorService.RUN_MODE == "test")
      val triedFile = pdfGeneratorService.generateCompliantPdfA(PdfGeneratorServiceIntegrationFixture.html)
      triedFile mustBe a[Success[File]]
    }
  }
}
