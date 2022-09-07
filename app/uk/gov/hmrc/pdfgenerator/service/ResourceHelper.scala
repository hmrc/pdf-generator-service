/*
 * Copyright 2022 HM Revenue & Customs
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

import java.io._

import play.api.Logging

import scala.io.Source
import scala.util.{Failure, Success, Try}
import scala.language.postfixOps

object ResourceHelper {
  def apply: ResourceHelper = new ResourceHelper()
}

class ResourceHelper extends Logging {

  val failureMessage = "Generation of pdfs will most likely fail!"

  def setupExecutableSupportFiles(
    psDefFileBare: String,
    psDefFileFullPath: String,
    baserDir: String,
    colorProfileBare: String,
    colorProfileFullPath: String): Unit = {

    def replace(line: String): String = line.replace("$COLOUR_PROFILE$", colorProfileFullPath)

    logger.info(s"Filtering pdf ${baserDir}conf/$psDefFileBare")
    val source = Source fromFile baserDir + "conf/" + psDefFileBare
    val lines = source.getLines
    val result = lines.map(line => replace(line))

    val file = new File(psDefFileFullPath)
    val bw = new BufferedWriter(new FileWriter(file))

    bw.write(result.mkString("\n"))

    if (!file.exists) {
      logger.error(s"$psDefFileFullPath does not exist! $failureMessage")
    }

    bw.close()
    source.close()

    copyFileAsBytes(colorProfileBare, colorProfileFullPath)

  }

  private def copyFileAsBytes(sourceFile: String, destinationFile: String): Unit =
    if (!new File(destinationFile).exists) {
      logger.info(s"Byte Copying $sourceFile to $destinationFile")
      val bytes = reader("/" + sourceFile)
      writer(destinationFile, bytes)
    }

  private def reader(filename: String): Try[Array[Byte]] = {
    val bis = new BufferedInputStream(getClass.getResourceAsStream(filename))
    val triedByteArray = Try(Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray)
    bis.close()
    logger.info(s"reading bytes for $filename : successful = ${triedByteArray.isSuccess}")
    triedByteArray
  }

  private def writer(filename: String, byteArray: Try[Array[Byte]]) = {
    val bos = new BufferedOutputStream(new FileOutputStream(filename))
    val tried = byteArray.map(byteArray => Stream.continually(bos.write(byteArray)))
    bos.close()

    tried match {
      case Success(t) => logger.info(s"Successfully wrote $filename")
      case Failure(e) => {
        logger.error(s"Failed to write bytes for $filename error was ${e.getMessage}, ")
      }
    }
  }

}
