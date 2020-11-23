package uk.gov.hmrc.pdfgenerator.controllers

import java.io.File

import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar.mock
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, _}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.pdfgenerator.metrics.PdfGeneratorMetric
import uk.gov.hmrc.pdfgenerator.resources._
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.test.UnitSpec

import scala.util.Try

class PdfGeneratorControllerSpec extends UnitSpec with GuiceOneAppPerTest with ScalaFutures with MockitoSugar {

  val mockMetric = mock[PdfGeneratorMetric]

  val pdfGeneratorController = new PdfGeneratorController(
    new MockPdfGeneratorService(configuration),
    stubControllerComponents(),
    mockMetric
  )

  when(mockMetric.startTimer()).thenReturn(1L)

  "POST /generate" should {
    "create pdf from a String of html sent in as a form element" in {
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
      val request = FakeRequest("POST", "/generate")
        .withFormUrlEncodedBody()

      val result = pdfGeneratorController.generate()(request).futureValue
      status(result) shouldBe Status.BAD_REQUEST

      contentAsString(result) shouldBe "html error.required"
    }
  }

}

class MockPdfGeneratorService(val configuration: Configuration)
    extends PdfGeneratorService(configuration, MockResourceHelper, Environment.simple()) {

  override def generatePdf(html: String, enableLinks: Boolean): Try[File] =
    Try {
      val testPdfFile = "PDFAcompliant.pdf"

      val file = new File(testPdfFile)

      if (!file.exists()) {
        throw new IllegalStateException(s"Can't find pdf for MockPdfGeneratorService $testPdfFile")
      }
      file
    }

}
