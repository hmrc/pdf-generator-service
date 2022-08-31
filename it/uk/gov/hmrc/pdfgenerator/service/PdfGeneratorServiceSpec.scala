package uk.gov.hmrc.pdfgenerator.service

import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.{Configuration, Environment, Mode}

import scala.util.Success



object PdfGeneratorServiceIntegrationFixture {
  def html : String = "<html><head></head><body><p>Hello</p></body></html>"
  def enableLinksFalse: Boolean = false
  def enableLinksTrue: Boolean = true
}

class PdfGeneratorServiceIntegrationSpec extends AnyWordSpec with Matchers with GuiceOneAppPerTest {


  val testConfig = new Configuration(ConfigFactory.load())
  val simple = Environment.simple()
  val environment = Environment.apply(simple.rootPath, simple.classLoader, Mode.Test)
  val pdfGeneratorService = new PdfGeneratorService(testConfig, ResourceHelper.apply, environment)

  "A PdfGeneratorService" should {
    "generate a pdf without links" in {
      val triedFile = pdfGeneratorService.generatePdf(PdfGeneratorServiceIntegrationFixture.html, PdfGeneratorServiceIntegrationFixture.enableLinksFalse)
      triedFile shouldBe a[Success[_]]
    }
    "generate a pdf with links" in {
      val triedFile = pdfGeneratorService.generatePdf(PdfGeneratorServiceIntegrationFixture.html, PdfGeneratorServiceIntegrationFixture.enableLinksTrue)
      triedFile shouldBe a[Success[_]]
    }
  }
}
