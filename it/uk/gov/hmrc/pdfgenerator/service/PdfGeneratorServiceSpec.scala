package uk.gov.hmrc.pdfgenerator.service

import java.io.File
import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.{Configuration, Environment, Mode}

import scala.util.Success



object PdfGeneratorServiceIntegrationFixture {
  def html : String = "<html><head></head><body><p>Hello</p></body></html>"
  def enableLinksFalse: Boolean = false
  def enableLinksTrue: Boolean = true
}

class PdfGeneratorServiceIntegrationSpec extends WordSpec with MustMatchers with GuiceOneAppPerTest {


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
  }
}
