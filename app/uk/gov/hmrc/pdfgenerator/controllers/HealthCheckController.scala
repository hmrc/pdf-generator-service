package uk.gov.hmrc.pdfgenerator.controllers

import javax.inject.Inject

import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.pdfgenerator.metrics.PdfGeneratorMetric
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.util.{Failure, Success}

class HealthCheckController @Inject()(val pdfGeneratorService: PdfGeneratorService) extends BaseController {

  def health = Action.async { implicit request =>
    val timer = PdfGeneratorMetric.startHealthCheckTimer
    pdfGeneratorService.generatePdf("<p>health</p>", true) match {
      case Success(file) => {
        file.delete()
        PdfGeneratorMetric.endHealthCheckTimer(timer)
        Future.successful(Ok)
      }
      case Failure(e) => {
        PdfGeneratorMetric.endHealthCheckTimer(timer)
        Logger.error("Pdf Service Failed HealthCheck", e)
        Future.successful(InternalServerError)
      }
    }
  }
}
