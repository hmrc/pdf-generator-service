package uk.gov.hmrc.pdfgenerator.controllers

import java.io.File
import javax.inject.Inject

import scala.util.Failure
import play.api.mvc._
import play.api.{Configuration, Logger}
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.util.Success

object PdfGeneratorController {
  @Inject()
  val configuration: Configuration = null
  def apply: PdfGeneratorController = new PdfGeneratorController(PdfGeneratorService.apply)
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
        pdfGeneratorService.generateCompliantPdfA(html) match {
          case Success(file) => Future.successful(Ok.sendFile(file, inline = false, onClose = () => file.delete()))
          case Failure(e) => Future.successful(BadRequest(e.getMessage))
        }
      }
    )
  }


}