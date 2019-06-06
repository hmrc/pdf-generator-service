package uk.gov.hmrc.pdfgenerator.service

import java.io.File
import java.nio.file.StandardCopyOption

import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, WordSpec}
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.play.test.WithFakeApplication

import scala.util.Success



object PdfGeneratorServiceIntegrationFixture {
  def html : String = "<html><head></head><body><p>Hello</p></body></html>"
  def enableLinksFalse: Boolean = false
  def enableLinksTrue: Boolean = true
}

class PdfGeneratorServiceIntegrationSpec extends WordSpec with MustMatchers with WithFakeApplication{


  val testConfig = new Configuration(ConfigFactory.load())
  val simple = Environment.simple()
  val environment = Environment.apply(simple.rootPath, simple.classLoader, Mode.Test)
  val pdfGeneratorService = new PdfGeneratorService(testConfig, ResourceHelper.apply, environment)

  "A PdfGeneratorService" should {
    "generate a pdf without links" in {
      val triedFile = pdfGeneratorService.generatePdf(PdfGeneratorServiceIntegrationFixture.html, PdfGeneratorServiceIntegrationFixture.enableLinksFalse)
      triedFile mustBe a[Success[File]]
    }
    "generate a pdf with links" in {
      val triedFile = pdfGeneratorService.generatePdf(PdfGeneratorServiceIntegrationFixture.html, PdfGeneratorServiceIntegrationFixture.enableLinksTrue)
      triedFile mustBe a[Success[File]]
    }
    "append pdf files together and clean up temporary files" in {
      def createPdfTmpFile = {
        val testPdf = new File("PDFAcompliant.pdf")

        val tmpPdf = SingletonTemporaryFileCreator.create("tmptest", ".pdf")
        java.nio.file.Files.copy(testPdf.toPath, tmpPdf.toPath, StandardCopyOption.REPLACE_EXISTING)
        tmpPdf.deleteOnExit()

        TemporaryFile(tmpPdf)
      }

      val tmpPdf1 = createPdfTmpFile
      val tmpPdf2 = createPdfTmpFile

      val triedFile = pdfGeneratorService.appendPdf(tmpPdf1, tmpPdf2)
      triedFile mustBe a[Success[File]]

      tmpPdf1.file.exists() mustBe false
      tmpPdf2.file.exists() mustBe false
    }
  }
}
