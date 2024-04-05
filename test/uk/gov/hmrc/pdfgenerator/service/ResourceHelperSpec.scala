/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.pdfgenerator.service

import java.io.File

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.io.Source

/**
  * Created by peter on 14/12/2016.
  */
class ResourceHelperSpec extends AnyWordSpec with Matchers {

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

      val lines: Iterator[String] = Source.fromFile(psDefFullpath).getLines()
      assert(
        lines.exists(line => line.contains(colorProfileFullPath)),
        "Should have filtered the color profile path into the def file")

    }
  }

  private def setup(filename: String): File = {
    val file = new File(psDefFullpath)
    file.deleteOnExit()
    if (file.exists()) file.delete()
    file
  }
}
