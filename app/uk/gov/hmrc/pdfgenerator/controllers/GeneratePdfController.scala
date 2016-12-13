package uk.gov.hmrc.pdfgenerator.controllers

import java.io.File
import java.util.UUID

import play.Logger
import play.api.libs.Files.TemporaryFile
import play.api.mvc._
import uk.gov.hmrc.pdfgenerator.controllers.InputFileValidator._
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService._
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.Future
import scala.sys.process.Process

object GeneratePdfController extends GeneratePdfController

trait GeneratePdfController extends BaseController {

	def generatePdfA() = Action.async { implicit request =>

		val multipartFormData: MultipartFormData[TemporaryFile] = request.body.asMultipartFormData.get

		val fileContents : String = validate(multipartFormData)

//		val inputFileName: String = "/app/" + UUID.randomUUID.toString + ".pdf"
//		val outputFileName: String = "/app/" +  UUID.randomUUID.toString + ".pdf"

		val inputFileName: String = UUID.randomUUID.toString + ".pdf"
		val outputFileName: String = UUID.randomUUID.toString + ".pdf"
    Logger.info("InputFileName initial is " + inputFileName)
    Logger.info("OutputFileName initial " + outputFileName)
		val command = "find  / -type d -name app "
		val command2 = "find  / -type d -name /app/ "
		val pb = Process(command)
		val pb2 = Process(command2)
		val exitCode = pb.!
		val exitCode2 = pb2.!
		val pdfA: File = generateCompliantPdfA(fileContents, inputFileName, outputFileName)


		Future.successful {
			Ok.sendFile(pdfA, inline = false)
		}
	}
}