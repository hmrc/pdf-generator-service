package uk.gov.hmrc.pdfgenerator.controllers

import javax.inject.Inject
import play.api.Logger
import play.api.mvc._
import uk.gov.hmrc.pdfgenerator.metrics.PdfGeneratorMetric
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.Future
import scala.util.{Failure, Success}

class HealthCheckController @Inject()(
  val pdfGeneratorService: PdfGeneratorService,
  pdfGenMetric: PdfGeneratorMetric,
  cc: ControllerComponents)
    extends BackendController(cc) {

  def health: Action[AnyContent] = Action.async { implicit request =>
    val timer = pdfGenMetric.startHealthCheckTimer()
    pdfGeneratorService.generatePdf("<p>health</p>", forcePdfA = true) match {
      case Success(file) =>
        file.delete()
        pdfGenMetric.endHealthCheckTimer(timer)
        Future.successful(Ok)
      case Failure(e) =>
        pdfGenMetric.endHealthCheckTimer(timer)
        Logger.error("Pdf Service Failed HealthCheck", e)
        Future.successful(InternalServerError)
    }
  }
}
