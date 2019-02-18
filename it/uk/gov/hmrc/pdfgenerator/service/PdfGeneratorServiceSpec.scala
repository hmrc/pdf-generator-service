package uk.gov.hmrc.pdfgenerator.service

import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, WordSpec}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.test.WithFakeApplication



object PdfGeneratorServiceIntegrationFixture {
  def html : String = "<html><head></head><body><p>Hello</p></body></html>"
}
`

class PdfGeneratorServiceIntegrationSpec extends WordSpec with MustMatchers with WithFakeApplication{


  val testConfig = new Configuration(ConfigFactory.load())
  val pdfGeneratorService = new PdfGeneratorService(testConfig, ResourceHelper.apply, Environment.simple())

  "A PdfGeneratorService" should {
    "generate a pdf" in {
      val triedFile = pdfGeneratorService.generateCompliantPdfA(PdfGeneratorServiceIntegrationFixture.html)
      assert(triedFile.isSuccess)
    }
  }



}
