package uk.gov.hmrc.pdfgenerator.controllers

import java.io.File
import java.nio.charset.Charset
import java.nio.file.StandardCopyOption

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.concurrent.ScalaFutures
import play.api.{Configuration, Environment}
import play.api.http.Status
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import com.kenshoo.play.metrics.PlayModule
import play.api.libs.Files.TemporaryFile
import play.api.mvc.{MultipartFormData, Result}
import play.api.mvc.MultipartFormData.FilePart
import uk.gov.hmrc.pdfgenerator.resources._

import scala.io.{Codec, Source}
import scala.util.Try


class PdfGeneratorControllerSpec extends UnitSpec with WithFakeApplication with ScalaFutures  {

  override def bindModules = Seq(new PlayModule)

  "POST /generate" should {
    "create pdf from a String of html sent in as a form element" in {
      val pdfGeneratorService = new MockPdfGeneratorService(configuration)
      val pdfGeneratorController = new PdfGeneratorController(pdfGeneratorService)

      val request = FakeRequest("POST", "/generate")
        .withFormUrlEncodedBody(
          "html" -> "<h1>Some html header</h1>"
        )

      val result = pdfGeneratorController.generate()(request).futureValue
      status(result) shouldBe Status.OK
      checkReturnedPdf(result, isSameAsPdf = pdfGeneratorService.testPdfFile.get)
    }

  }

  "POST /generate" should {
    "return an error if the html form element is not present" in {
      val pdfGeneratorController = new PdfGeneratorController(new MockPdfGeneratorService(configuration))

      val request = FakeRequest("POST", "/generate")
        .withFormUrlEncodedBody()

      val result = pdfGeneratorController.generate()(request).futureValue
      status(result) shouldBe Status.BAD_REQUEST

      contentAsString(result) shouldBe "html error.required"
    }
  }

  "POST /append" should {
    "create a pdf from two pdf files uploaded via multipart/form-data" in {
      val pdfGeneratorService = new MockPdfGeneratorService(configuration)
      val pdfGeneratorController = new PdfGeneratorController(pdfGeneratorService)

      val firstFile = TemporaryFile(file = File.createTempFile("test", ".pdf"))
      val secondFile = TemporaryFile(file = File.createTempFile("test", ".pdf"))
      val firstFilePart = FilePart(key = "", filename = "", contentType = Some("application/pdf"), ref = firstFile)
      val secondFilePart = FilePart(key = "", filename = "", contentType = Some("application/pdf"), ref = secondFile)
      val formData = MultipartFormData[TemporaryFile](
        dataParts = Map.empty,
        files = Seq(firstFilePart, secondFilePart),
        badParts = Nil
      )
      val request = FakeRequest("POST", "/append", FakeHeaders(), formData)

      val result = pdfGeneratorController.append(request)

      status(result) shouldBe Status.OK
      checkReturnedPdf(fromResult = result, isSameAsPdf = pdfGeneratorService.testPdfFile.get)
    }

    "return an error if the multipart/form-data contains no files" in {
      val pdfGeneratorController = new PdfGeneratorController(new MockPdfGeneratorService(configuration))

      val formData = MultipartFormData[TemporaryFile](
        dataParts = Map.empty,
        files = Nil,
        badParts = Nil
      )
      val request = FakeRequest("POST", "/append", FakeHeaders(), formData)

      val result = pdfGeneratorController.append(request)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "Two or more PDF files are required for appending"
    }

    "return an error if the multipart/form-data contains files that are not PDFs" in {
      val pdfGeneratorController = new PdfGeneratorController(new MockPdfGeneratorService(configuration))

      val firstFile = TemporaryFile(file = File.createTempFile("test", ".txt"))
      val secondFile = TemporaryFile(file = File.createTempFile("test", ".txt"))
      val firstFilePart = FilePart(key = "", filename = "", contentType = Some("text/plain"), ref = firstFile)
      val secondFilePart = FilePart(key = "", filename = "", contentType = Some("text/plain"), ref = secondFile)
      val formData = MultipartFormData[TemporaryFile](
        dataParts = Map.empty,
        files = Seq(firstFilePart, secondFilePart),
        badParts = Nil
      )
      val request = FakeRequest("POST", "/append", FakeHeaders(), formData)

      val result = pdfGeneratorController.append(request)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe "Unexpected content-type of 'Some(text/plain)' for file ''/''. Expected 'application/pdf'."
    }
  }

  def checkReturnedPdf(fromResult: Result, isSameAsPdf: File) = {
    implicit val sys = ActorSystem("JustForThisTest")
    implicit val mat = ActorMaterializer()
    val expectedPdfHead = Source.fromFile(isSameAsPdf)(Codec.ISO8859).mkString
    val actualPdfHead = await(fromResult.body.consumeData).decodeString(Charset.forName("ISO-8859-1"))

    actualPdfHead shouldBe expectedPdfHead
  }

}

class MockPdfGeneratorService (val configuration: Configuration) extends PdfGeneratorService(configuration, MockResourceHelper, Environment.simple()) {

  override def generatePdf(html: String, enableLinks: Boolean): Try[File] = testPdfFile

  override def appendPdf(pdfFiles: TemporaryFile*): Try[File] = testPdfFile

  def testPdfFile = {
    Try {
      val file = new File("PDFAcompliant.pdf")

      if (!file.exists()) {
        throw new IllegalStateException(s"Can't find pdf for MockPdfGeneratorService ${file.getName}")
      }

      copyToTmpFile(file)
    }
  }

  private def copyToTmpFile(file: File): File = {
    val tmpPdfCopy = File.createTempFile("tmp", file.getName)
    java.nio.file.Files.copy(file.toPath, tmpPdfCopy.toPath, StandardCopyOption.REPLACE_EXISTING)
    tmpPdfCopy.deleteOnExit()

    tmpPdfCopy
  }

}