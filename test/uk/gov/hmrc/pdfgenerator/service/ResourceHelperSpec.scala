package uk.gov.hmrc.pdfgenerator.service

import java.io.File

import org.scalatest.{MustMatchers, WordSpec}

import scala.io.Source

/**
  * Created by peter on 14/12/2016.
  */
class ResourceHelperSpec extends WordSpec with MustMatchers {

  private val localDir = new File(".").getCanonicalPath + "/"

  private val psDef: String = "PDFA_def.ps"

  val psDefFullpath: String = localDir + "target/" + psDef

  val basDir: String = localDir

  var colorProfile: String = "AdobeRGB1998.icc"

  var colorProfileFullPath: String = localDir + "target/" + colorProfile

  "A resoucer helper " should {

    "setup the wktohtml and ghostscript runtime support files " in {

      val testPsDefFile = setup(psDefFullpath)
      val testColorProfileFile = setup(colorProfileFullPath)

      ResourceHelper.apply.setupExecutableSupportFiles(psDef, psDefFullpath, basDir, colorProfile, colorProfileFullPath)

      assert(testPsDefFile.exists(), "PS Def file should exist")
      assert(testColorProfileFile.exists(), "Color Profile File should exist")

      val lines: Iterator[String] = Source fromFile psDefFullpath getLines()
      assert(lines.exists( line => line.contains(colorProfileFullPath)), "Should have filtered the color profile path into the def file")


    }
  }



  private def setup(filename: String): File = {
    val file = new File(psDefFullpath)
    file.deleteOnExit()
    if (file.exists()) file.delete()
    file
  }
}
