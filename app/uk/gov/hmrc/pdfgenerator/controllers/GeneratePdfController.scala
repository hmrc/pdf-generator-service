package uk.gov.hmrc.pdfgenerator.controllers

import java.io.File
import java.util.UUID

import play.api.libs.Files.TemporaryFile
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.microservice.controller.BaseController
import play.api.mvc._
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService._
import InputFileValidator._

import scala.concurrent.Future

object GeneratePdfController extends GeneratePdfController

trait GeneratePdfController extends BaseController {

	def generatePdfA() = Action.async { implicit request =>

		val multipartFormData: MultipartFormData[TemporaryFile] = request.body.asMultipartFormData.get

		val fileContents : String = validate(multipartFormData)

		val inputFileName: String = "/app/" + UUID.randomUUID.toString + ".pdf"
		val outputFileName: String = "/app/" +  UUID.randomUUID.toString + ".pdf"

		val pdfA: File = generateCompliantPdfA(fileContents, inputFileName, outputFileName)


		Future.successful {
			Ok.sendFile(pdfA, inline = false)
		}
	}
}