package uk.gov.hmrc.pdfgenerator.controllers

import javax.inject.Inject

import play.api.mvc._
import play.api.{Configuration, Logger}
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.util.{Failure, Success}


object HealthCheckController {
  @Inject()
  val configuration: Configuration = null
  def apply: HealthCheckController = new HealthCheckController(PdfGeneratorService.apply)
}

class HealthCheckController @Inject()(val pdfGeneratorService: PdfGeneratorService) extends BaseController {

  def health = Action.async { implicit request =>
    pdfGeneratorService.generateCompliantPdfA("<p>health</p>") match {
      case Success(file) => {
        file.delete()
        Future.successful(Ok)
      }
      case Failure(e) => {
        Logger.error("Failed HealthCheckController", e)
        Future.successful(InternalServerError)
      }
    }
  }
}