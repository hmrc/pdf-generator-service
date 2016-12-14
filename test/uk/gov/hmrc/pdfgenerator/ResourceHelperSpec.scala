package uk.gov.hmrc.pdfgenerator

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.pdfgenerator.service.ResourceHelper

/**
  * Created by peter on 14/12/2016.
  */
class ResourceHelperSpec extends WordSpec with MustMatchers {

  val resourceHelper = new ResourceHelper()


  "A resoucer helper " should {

    "read a a file as a byte array and write is back out as a byte arry" in {
      resourceHelper.writer("./testAdobeRGB1998.icc", resourceHelper.reader("/AdobeRGB1998.icc"))
    }
  }

}
