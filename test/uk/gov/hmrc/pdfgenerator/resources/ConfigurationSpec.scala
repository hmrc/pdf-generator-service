package uk.gov.hmrc.pdfgenerator.resources

import org.scalatest.{MustMatchers, WordSpec}
import uk.gov.hmrc.pdfgenerator.service.ResourceHelper
import uk.gov.hmrc.pdfgenerator.resources._

import scala.io.Source

/**
  * Created by peter on 27/03/2017.
  */
class ConfigurationSpec extends WordSpec with MustMatchers {

  "The configuration " should {
    "contain the dev values for the executables " in {
      assert(configuration.getString("pdfGeneratorService.gsAlias").isDefined)
      assert(configuration.getString("pdfGeneratorService.psDef").isDefined)
      assert(configuration.getBoolean("pdfGeneratorService.baseDirDevMode").isDefined)
      assert(!configuration.getBoolean("RUBBISH").isDefined)

    }
  }

}
