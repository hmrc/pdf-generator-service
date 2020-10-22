package uk.gov.hmrc.pdfgenerator.controllers

import java.io.{File, IOException}

import com.codahale.metrics.SharedMetricRegistries
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.pdfgenerator.metrics.PdfGeneratorMetric
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.test.UnitSpec

import scala.util.Try

class HealthCheckControllerSpec extends UnitSpec with GuiceOneAppPerTest with ScalaFutures with MockitoSugar {

  val mockPdfGeneratorService: PdfGeneratorService = mock[PdfGeneratorService]
  val mockMetric: PdfGeneratorMetric = mock[PdfGeneratorMetric]

  val healthCheckController = new HealthCheckController(
    mockPdfGeneratorService,
    mockMetric,
    stubControllerComponents()
  )

  when(mockMetric.startHealthCheckTimer()).thenReturn(1L)

  private val mockFile = new File("./target/testFileToDelete")
  mockFile.createNewFile()
  val triedFile: Try[File] = Try(mockFile)

  "A HealthCheckController" should {
    "return 200 OK when all is fine" in {
      when(mockPdfGeneratorService.generatePdf("<p>health</p>", forcePdfA = true)).thenReturn(triedFile)
      val request = FakeRequest("GET", "/healthcheck/")
      val result = await(healthCheckController.health.apply(request))
      status(result) shouldBe Status.OK
    }
  }

  "A HealthCheckController" should {
    "return 500 when there are issues building a pdf" in {
      when(mockPdfGeneratorService.generatePdf("<p>health</p>", forcePdfA = true)).thenReturn(badTry)
      val request = FakeRequest("GET", "/healthcheck/")
      val result = await(healthCheckController.health.apply(request))
      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }
  }

  def badTry: Try[Nothing] = Try {
    throw new IOException("Mocking something going wrong")
  }

}
