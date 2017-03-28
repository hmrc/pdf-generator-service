package uk.gov.hmrc.pdfgenerator.service

import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, WordSpec}
import play.api.Configuration
import uk.gov.hmrc.pdfgenerator.resources._


object PdfGeneratorServiceIntegrationFixture {
  def html : String = "<html><head></head><body><p>Hello</p></body></html>"
}


class PdfGeneratorServiceIntegrationSpec extends WordSpec with MustMatchers{

  val pdfGeneratorService = new PdfGeneratorService(configuration, MockResourceHelper)

  "A PdfGeneratorService" should {
    "generate a pdf" in {
      val triedFile = pdfGeneratorService.generateCompliantPdfA(PdfGeneratorServiceIntegrationFixture.html)
      assert(triedFile.isSuccess)
    }
  }



}
