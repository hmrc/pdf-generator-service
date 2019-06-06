package uk.gov.hmrc.pdfgenerator.controllers

import java.io.File
import javax.inject.Inject
import play.api.libs.Files.TemporaryFile

import scala.util.{Failure, Success, Try}
import play.api.mvc._
import play.api.{Configuration, Logger}
import uk.gov.hmrc.pdfgenerator.metrics.PdfGeneratorMetric
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

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
        val triedPdfFile = pdfGeneratorService.generatePdf(pdf.html, pdf.forcePdfA)
        handleGeneratedPdf(triedPdfFile, start)
      }
    )
  }

  def appendAction = Action.async(parse.multipartFormData) { implicit request =>
    append(request)
  }

  def append(request: Request[MultipartFormData[TemporaryFile]]): Future[Result] = {
    Logger.debug("******* Appending PDFs ***********")

    val start = PdfGeneratorMetric.startTimer()

    def hasCorrectContentType(filePart: MultipartFormData.FilePart[TemporaryFile]) = {
      filePart.contentType.map(_.toLowerCase).contains("application/pdf")
    }

    val hasEnoughFiles = request.body.files.size >= 2

    if(!hasEnoughFiles) {
      Future.successful(BadRequest("Two or more PDF files are required for appending"))
    } else {
      request.body.files.find(!hasCorrectContentType(_)) match {
        case Some(badFile) => {
          Future.successful(BadRequest(s"Unexpected content-type of '${badFile.contentType}' for file '${badFile.key}'/'${badFile.filename}'. Expected 'application/pdf'."))
        }
        case _ => {
          val files = request.body.files.map(_.ref)
          val triedPdfOutput = pdfGeneratorService.appendPdf(files :_*)
          handleGeneratedPdf(triedPdfOutput, start)
        }
      }
    }
  }

  private def handleGeneratedPdf(triedPdfFile: Try[File], startMetricMs: Long): Future[Result] = {
    triedPdfFile match {
      case Success(file) => {
        PdfGeneratorMetric.successCount()
        PdfGeneratorMetric.endTimer(startMetricMs)
        Future.successful(Ok.sendFile(file, inline = false, onClose = () => file.delete()))
      }
      case Failure(e) => {
        PdfGeneratorMetric.failureCount()
        PdfGeneratorMetric.endTimer(startMetricMs)
        Future.successful(BadRequest(e.getMessage))
      }
    }
  }
}