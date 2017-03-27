package uk.gov.hmrc.pdfgenerator.service

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.pdfgenerator.resources._

object PdfGeneratorServiceFixture {
  def html : String = "<html><head></head><body><p>Hello</p></body></html>"
}


class PdfGeneratorServiceSpec extends WordSpec with MustMatchers{

  val pdfGeneratorService = new PdfGeneratorService(configuration, MockResourceHelper)

  "A PdfGeneratorService" should {
    "generate a pdf" in {
      val triedFile = pdfGeneratorService.generateCompliantPdfA(PdfGeneratorServiceFixture.html)
      assert(triedFile.isSuccess)
    }
  }
}
