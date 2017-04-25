package uk.gov.hmrc.pdfgenerator.controllers

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.concurrent.ScalaFutures
import play.api.Configuration
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.pdfgenerator.service.{PdfGeneratorService, ResourceHelper}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import com.kenshoo.play.metrics.PlayModule

import uk.gov.hmrc.pdfgenerator.resources._

import scala.util.Try


class PdfGeneratorControllerSpec extends UnitSpec with WithFakeApplication with ScalaFutures  {

  override def bindModules = Seq(new PlayModule)



  "POST /generate" should {
    "create pdf from a String of html sent in as a form element" in {
      val pdfGeneratorController = new PdfGeneratorController(new MockPdfGeneratorService(configuration))

      val request = FakeRequest("POST", "/generate")
        .withFormUrlEncodedBody(
          "html" -> "<h1>Some html header</h1>"
        )

      val result = pdfGeneratorController.generate()(request).futureValue
      status(result) shouldBe Status.OK
      //todo validate the result
    }

  }

  "POST /generate" should {
    "return an error if the html form element is not present" in {
      val pdfGeneratorController = new PdfGeneratorController(new MockPdfGeneratorService(configuration))

      val request = FakeRequest("POST", "/generate")
        .withFormUrlEncodedBody()

      val result = pdfGeneratorController.generate()(request).futureValue
      status(result) shouldBe Status.BAD_REQUEST

      contentAsString(result) shouldBe "html error.required"
    }
  }

}



class MockPdfGeneratorService (val configuration: Configuration) extends PdfGeneratorService(configuration, MockResourceHelper) {

  override def generateCompliantPdfA(html: String): Try[File] = {

    Try {
      val testPdfFile = "PDFAcompliant.pdf"

      val file = new File(testPdfFile)

      if (!file.exists()) {
        throw new IllegalStateException(s"Can't find pdf for MockPdfGeneratorService ${testPdfFile}")
      }
      file
    }
  }


}
