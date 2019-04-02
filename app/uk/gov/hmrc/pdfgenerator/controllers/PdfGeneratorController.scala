package uk.gov.hmrc.pdfgenerator.controllers

import java.io.File
import javax.inject.Inject

import scala.util.Failure
import play.api.mvc._
import play.api.{Configuration, Logger}
import uk.gov.hmrc.pdfgenerator.metrics.PdfGeneratorMetric
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.util.Success

class PdfGeneratorController @Inject()(val pdfGeneratorService: PdfGeneratorService) extends BaseController with HtmlSupport {


  def generate = Action.async { implicit request =>

    Logger.debug("******* Generating PDF ***********")

    val start = PdfGeneratorMetric.startTimer()

    val pdfForm = getPdfForm()

    pdfForm.bindFromRequest.fold(
      badRequest => {
        val errors = badRequest.errors.map(formError => formError.key + " " + formError.messages.mkString(" ")).mkString(" : ")
        Future.successful(BadRequest(errors))
      },
      pdf => {
        pdfGeneratorService.generatePdf(pdf.html,pdf.createPdfA) match {
          case Success(file) => {
            PdfGeneratorMetric.successCount()
            PdfGeneratorMetric.endTimer(start)
            Future.successful(Ok.sendFile(file, inline = false, onClose = () => file.delete()))
          }
          case Failure(e) => {
            PdfGeneratorMetric.failureCount()
            PdfGeneratorMetric.endTimer(start)
            Future.successful(BadRequest(e.getMessage))
          }
        }
      }
    )
  }


}