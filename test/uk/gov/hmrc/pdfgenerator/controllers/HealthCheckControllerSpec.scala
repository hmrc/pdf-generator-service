package uk.gov.hmrc.pdfgenerator.controllers

import java.io.{File, IOException}

import com.kenshoo.play.metrics.PlayModule
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.http.Status
import play.api.test.FakeRequest
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito.when

import scala.util.Try

class HealthCheckControllerSpec extends UnitSpec with WithFakeApplication with ScalaFutures with MockitoSugar {

  override def bindModules = Seq(new PlayModule)

  val mockPdfGeneratorService = mock[PdfGeneratorService]

  val healthCheckController = new HealthCheckController(mockPdfGeneratorService)

  private val mockFile = new File("./target/testFileToDelete")
  mockFile.createNewFile()
  val triedFile = Try(mockFile)

  "A HealthCheckController" should {
    "return 200 OK when all is fine" in {

      when(mockPdfGeneratorService.generatePdf("<p>health</p>")).thenReturn(triedFile)
      val request = FakeRequest("GET", "/healthcheck/")
      val result = await(healthCheckController.health.apply(request))
      status(result) shouldBe Status.OK
    }
  }

  "A HealthCheckController" should {
    "return 500 when there are issues building a pdf" in {

      when(mockPdfGeneratorService.generatePdf("<p>health</p>")).thenReturn(badTry)
      val request = FakeRequest("GET", "/healthcheck/")
      val result = await(healthCheckController.health.apply(request))
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  def badTry = Try {
    throw new IOException("Mocking something going wrong")
  }


}

