package uk.gov.hmrc.pdfgenerator

import java.io.File
import java.util.UUID

import org.apache.pdfbox.pdmodel.PDDocument
import org.scalatest.{MustMatchers, WordSpec}
import PdfGeneratorServiceFixture._
import org.apache.pdfbox.preflight.{Format, PreflightDocument, ValidationResult}
import org.apache.pdfbox.preflight.parser.PreflightParser
import org.apache.pdfbox.text.PDFTextStripper
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService._
import uk.gov.hmrc.play.http.BadRequestException

object PdfGeneratorServiceFixture{

  def html : String = "<html><head></head><body><p>Hello</p></body></html>"

}

/**
  * Created by habeeb on 14/10/2016.
  */

class PdfGeneratorServiceSpec extends WordSpec with MustMatchers{


  "PdfGeneratorService" should{

    "not generate PDF with null HTML" in {
      val thrown = intercept[BadRequestException] {
        generatePdfFromHtml(null, "outputFileName.pdf")
      }
      thrown.getMessage must equal("Html must be provided")
    }

    "not generate PDF with HTML as empty string" in {
      val thrown = intercept[BadRequestException] {
        generatePdfFromHtml("", "outputFileName.pdf")
      }
      thrown.getMessage must equal("Html must be provided")
    }

    "not generate PDF with null Output file name" in {
      val thrown = intercept[BadRequestException] {
        generatePdfFromHtml(html, null)
      }
      thrown.getMessage must equal("OutputFileName must be provided")
    }

    "not generate PDF with Output file name as empty string" in {
      val thrown = intercept[BadRequestException] {
        generatePdfFromHtml(html, "")
      }
      thrown.getMessage must equal("OutputFileName must be provided")
    }

    "generate a PDF from the given HTML" in {
      val outputFileName: String = "output"+UUID.randomUUID().toString+".pdf"
      generatePdfFromHtml(html, outputFileName)
      val fullFilePath: String = new File(".").getCanonicalPath + "/" + outputFileName

      getFileContents(fullFilePath).trim must equal("Hello")

      deleteFile(fullFilePath)
    }

    "convert PDF to PDF/A" in {

      val inputFilename: String = "non-compliant.pdf"
      val outputFilename: String = "PDFAcompliant.pdf"

      assertPdfIsPdfaCompliant(inputFilename) mustBe false

      convertToPdfA(inputFilename, outputFilename)

      assertPdfIsPdfaCompliant(outputFilename) mustBe true

      //teardown
      deleteFile(outputFilename)
    }

  }

  def getFileContents(filename : String) : String = {

    val pdf = PDDocument.load(new File(filename))
    val stripper = new PDFTextStripper()
    stripper.setStartPage(stripper.getStartPage)
    stripper.setEndPage(stripper.getEndPage)

    return stripper.getText(pdf)
  }

  def deleteFile(file : String) = {
    import scala.sys.process.Process

    val deleteCommand: String = "rm -Rf" + " " + file

    val pd = Process(deleteCommand)

    val exitCodeTwo = pd.!
  }

  def assertPdfIsPdfaCompliant(fileName : String) : Boolean = {
    val parser: PreflightParser = new PreflightParser(baseDir + fileName)
    parser.parse(Format.PDF_A1B)

    val preflightDocument: PreflightDocument = parser.getPreflightDocument
    preflightDocument.validate()

    val result: ValidationResult = preflightDocument.getResult

    return result.isValid
  }


}
