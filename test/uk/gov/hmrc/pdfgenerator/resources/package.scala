package uk.gov.hmrc.pdfgenerator

import com.typesafe.config.ConfigFactory
import play.api.Configuration
import uk.gov.hmrc.pdfgenerator.service.ResourceHelper

/**
  * Created by peter on 27/03/2017.
  */
package object resources {

  val configuration = new Configuration(ConfigFactory.load("application.conf"))

  object MockResourceHelper extends ResourceHelper {
    override def setupExecutableSupportFiles(
      psDefFileBare: String,
      psDefFileFullPath: String,
      baserDir: String,
      colorProfileBare: String,
      colorProfileFullPath: String): Unit = {}
  }
}
