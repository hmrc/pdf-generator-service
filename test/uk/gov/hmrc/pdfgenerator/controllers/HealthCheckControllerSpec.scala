/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.pdfgenerator.controllers

import java.io.{File, IOException}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.http.Status
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.pdfgenerator.metrics.PdfGeneratorMetric
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.pdfgenerator.utils.PdfGeneratorUnitSpec

import scala.util.Try

class HealthCheckControllerSpec extends PdfGeneratorUnitSpec with GuiceOneAppPerTest with ScalaFutures {

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
