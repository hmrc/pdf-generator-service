/*
 * Copyright 2022 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.Logging
import play.api.mvc._
import uk.gov.hmrc.pdfgenerator.metrics.PdfGeneratorMetric
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.Future
import scala.util.{Failure, Success}

class HealthCheckController @Inject()(
  val pdfGeneratorService: PdfGeneratorService,
  pdfGenMetric: PdfGeneratorMetric,
  cc: ControllerComponents)
    extends BackendController(cc) with Logging {

  def health: Action[AnyContent] = Action.async { _ =>
    val timer = pdfGenMetric.startHealthCheckTimer()
    pdfGeneratorService.generatePdf("<p>health</p>", forcePdfA = true) match {
      case Success(file) =>
        file.delete()
        pdfGenMetric.endHealthCheckTimer(timer)
        Future.successful(Ok)
      case Failure(e) =>
        pdfGenMetric.endHealthCheckTimer(timer)
        logger.error("Pdf Service Failed HealthCheck", e)
        Future.successful(InternalServerError)
    }
  }
}
