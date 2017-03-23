package uk.gov.hmrc.pdfgenerator.controllers

import java.io.File
import javax.inject.Inject

import play.api.mvc._
import play.api.{Configuration, Logger}
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future

object PdfGeneratorController {
  @Inject()
  val configuration: Configuration = null
  def apply: PdfGeneratorController = new PdfGeneratorController(new PdfGeneratorService(configuration))
}


class PdfGeneratorController @Inject()(val pdfGeneratorService: PdfGeneratorService) extends BaseController with HtmlSupport {


  def generate = Action.async { implicit request =>

    Logger.debug("******* Generating PDF ***********")
    val htmlForm = createForm()

    htmlForm.bindFromRequest.fold(
      badRequest => {
        val errors = badRequest.errors.map(formError => formError.key + " " + formError.messages.mkString(" ")).mkString(" : ")
        Future.successful(BadRequest(errors))
      },
      html => {
        val pdfA: File = pdfGeneratorService.generateCompliantPdfA(html) // todo make this an Option
        Future.successful(Ok.sendFile(pdfA, inline = false, onClose = () => pdfA.delete()))
      }
    )
  }


}