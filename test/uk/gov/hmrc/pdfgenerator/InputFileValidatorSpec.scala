package uk.gov.hmrc.pdfgenerator

import java.net.{URI, URL}

import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.MultipartFormData
import java.io.{File, FileInputStream}
import java.util.Properties

import uk.gov.hmrc.play.http.BadRequestException
import org.scalatest.{MustMatchers, WordSpec}

import scala.io.Source

object MultipartFormFixture {

  def getFileFromClasspath(name: String): File = {
    new File (getClass.getResource("/" + name).toURI)
  }

  private val noScriptTagfile: File = getFileFromClasspath("test.htm")
  private val scripTagfile: File = getFileFromClasspath("test_withScriptTag.htm")
  private val dummyFile: File = getFileFromClasspath("dummy.txt")
  private val PDFAdef_psFile: File = getFileFromClasspath("PDFA_def.ps")

  val pdfaFile: String = Source.fromFile(PDFAdef_psFile).mkString

  def completeMultipartFormData_noScriptTags: MultipartFormData[TemporaryFile] = MultipartFormData(
    dataParts = Map(),
    files = Seq(FilePart[TemporaryFile]("file", noScriptTagfile.getName, Some("text/html"), TemporaryFile(noScriptTagfile))),
    badParts = Seq())

  def completeMultipartFormData_withScriptTags: MultipartFormData[TemporaryFile] = MultipartFormData(
    dataParts = Map(),
    files = Seq(FilePart[TemporaryFile]("file", scripTagfile.getName, Some("text/html"), TemporaryFile(scripTagfile))),
    badParts = Seq())

  def dummyFileMultipartForm: MultipartFormData[TemporaryFile] = MultipartFormData(
    dataParts = Map(),
    files = Seq(FilePart[TemporaryFile]("file", dummyFile.getName, Some("text/html"), TemporaryFile(dummyFile))),
    badParts = Seq())

  def emptyMultipartFormData_withScriptTags: MultipartFormData[TemporaryFile] = MultipartFormData(
    dataParts = Map(),
    files = Seq(),
    badParts = Seq())

}

/**
  * Created by habeeb on 14/10/2016.
  */
class InputFileValidatorSpec extends WordSpec with MustMatchers{

  import uk.gov.hmrc.pdfgenerator.controllers.InputFileValidator._
  import uk.gov.hmrc.pdfgenerator.MultipartFormFixture._


  "InputFileValidator" should{

    "not accept a multipart form with no file" in {
      val thrown = intercept[BadRequestException] {
        validate(emptyMultipartFormData_withScriptTags)
      }
      thrown.getMessage must equal("Please select a file")
    }

    "not accept a multipart form with a file that contains a script tag" in {
      val thrown = intercept[BadRequestException] {
        validate(completeMultipartFormData_withScriptTags)
      }
      thrown.getMessage must equal("HTML must not contain any script tags")
    }

    "not accept a multipart form with a file that is not HTML" in {
      val thrown = intercept[BadRequestException] {
        validate(dummyFileMultipartForm)
      }
      thrown.getMessage must equal("File must be an HMTL file")
    }

    "accept a multipart form with a valid HTML file" in {
      validate(completeMultipartFormData_noScriptTags) mustBe a [String]
    }

    "not accept a .ps file without path to .icc profile" in {
      val thrown = intercept [BadRequestException] {
      validatePDFA(pdfaFile)
      }
      thrown.getMessage must equal("File must contain path to icc profile")
    }

    "not accept a blank .ps file" in {
      val thrown = intercept [BadRequestException] {
        validatePDFANotEmpty(pdfaFile)
      }
      thrown.getMessage must equal (".ps file is blank")
    }

  }

}
