/*
 * Copyright 2023 HM Revenue & Customs
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
import scala.util.Failure
import play.api.mvc._
import play.api.Logging
import uk.gov.hmrc.pdfgenerator.metrics.PdfGeneratorMetric
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class PdfGeneratorController @Inject()(
  val pdfGeneratorService: PdfGeneratorService,
  cc: ControllerComponents,
  pdfGeneratorMetric: PdfGeneratorMetric)
    extends BackendController(cc) with HtmlSupport with Logging {

  implicit val ec: ExecutionContext = cc.executionContext

  def generate: Action[AnyContent] = Action.async { implicit request =>
    logger.info("******* Generating PDF ***********")

    val start = pdfGeneratorMetric.startTimer()

    val pdfForm = getPdfForm()

    pdfForm
      .bindFromRequest()
      .fold(
        badRequest => {
          val errors =
            badRequest.errors.map(formError => formError.key + " " + formError.messages.mkString(" ")).mkString(" : ")
          Future.successful(BadRequest(errors))
        },
        pdf => {
          pdfGeneratorService.generatePdf(pdf.html, pdf.forcePdfA) match {
            case Success(file) =>
              pdfGeneratorMetric.successCount()
              pdfGeneratorMetric.endTimer(start)
              Future.successful(Ok.sendFile(file, inline = false, onClose = () => file.delete()))
            case Failure(e) =>
              pdfGeneratorMetric.failureCount()
              pdfGeneratorMetric.endTimer(start)
              Future.successful(BadRequest(e.getMessage))
          }
        }
      )
  }

}
