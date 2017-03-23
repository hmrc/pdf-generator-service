package uk.gov.hmrc.pdfgenerator

import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import org.scalatest.{MustMatchers, WordSpec}

object PdfGeneratorServiceFixture{

  def html : String = "<html><head></head><body><p>Hello</p></body></html>"

}

/**
  * Created by habeeb on 14/10/2016.
  */

class PdfGeneratorServiceSpec extends WordSpec with MustMatchers{

  val pdfGeneratorService = PdfGeneratorService
}
