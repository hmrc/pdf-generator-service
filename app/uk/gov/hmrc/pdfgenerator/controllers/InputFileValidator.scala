package uk.gov.hmrc.pdfgenerator.controllers

import java.io.File

import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{Action, MultipartFormData}
import uk.gov.hmrc.play.http.BadRequestException

import scala.concurrent.Future
import scala.io.{ Source}


object InputFileValidator extends InputFileValidator

/**
  * Created by habeeb on 10/10/2016.
  *
  * This class is responsible for validating the submitted file
  *
  */
trait InputFileValidator {

  def validate(multipartFormData: MultipartFormData[TemporaryFile]): String = {

    validateNotEmpty(multipartFormData)

    val filePart: FilePart[TemporaryFile] = multipartFormData.files.head

    validateFileExtension(filePart.filename.split('.').last)

    val file: File = filePart.ref.file

    val source = Source.fromFile(file)
    val lines = try source.mkString finally source.close()

    checkForScriptTags(lines)

    lines
  }


  def validatePDFA(pdfaFile: String) = {
    checkForICCProfile(pdfaFile)
  }

  def validatePDFANotEmpty(pdfaFile: String) = {
    checkForContentInPsFile(pdfaFile)
  }


  def validateNotEmpty(multipartFormData: MultipartFormData[TemporaryFile]) = {

    if (multipartFormData.files.isEmpty) {
      Future.failed(throw new BadRequestException("Please select a file"))
    }
  }

  def checkForICCProfile(content: String) = {
    if (content.contains(".icc")) {
      Future.failed(throw new BadRequestException("File must contain path to icc profile"))
    }
  }

  def checkForContentInPsFile(content: String) = {
    if (!content.isEmpty) {
      Future.failed(throw new BadRequestException(".ps file is blank"))
    }
  }

  def checkForScriptTags(fileContent: String) = {
    if (fileContent.toLowerCase.contains("<script>")) {
      Future.failed(throw new BadRequestException("HTML must not contain any script tags"))
    }
  }

  def validateFileExtension(fileExtension: String) = {
    if (!fileExtension.equals("html") && !fileExtension.equals("htm")) {
      Future.failed(throw new BadRequestException("File must be an HMTL file"))
    }
  }
}


