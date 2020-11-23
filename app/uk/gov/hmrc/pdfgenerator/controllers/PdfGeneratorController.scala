package uk.gov.hmrc.pdfgenerator.controllers

import java.io.File

import javax.inject.Inject

import scala.util.Failure
import play.api.mvc._
import play.api.Logger
import uk.gov.hmrc.pdfgenerator.metrics.PdfGeneratorMetric
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.bootstrap.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class PdfGeneratorController @Inject()(
  val pdfGeneratorService: PdfGeneratorService,
  cc: ControllerComponents,
  pdfGeneratorMetric: PdfGeneratorMetric)
    extends BackendController(cc) with HtmlSupport {

  implicit val ec: ExecutionContext = cc.executionContext

  def generate: Action[AnyContent] = Action.async { implicit request =>
    Logger.debug("******* Generating PDF ***********")

    val start = pdfGeneratorMetric.startTimer()

    val pdfForm = getPdfForm()

    pdfForm.bindFromRequest.fold(
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
