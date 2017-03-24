package uk.gov.hmrc.pdfgenerator.service

import java.io._

import play.api.Logger

import scala.io.Source

object ResourceHelper {
  def apply: ResourceHelper = new ResourceHelper()
}

class ResourceHelper {

  def reader(filename: String): Array[Byte]  = {
    val bis = new BufferedInputStream(getClass.getResourceAsStream(filename))
    try Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
    finally bis.close()
  }

  def writer(filename: String, byteArray: Array[Byte]) = {
    val bos = new BufferedOutputStream(new FileOutputStream(filename))
    Stream.continually(bos.write(byteArray))
    bos.close()
  }


  def setUpPsDefFile(pdDefFileBare: String,
                     pdDefFileFullPath: String, baserDir: String,
                     colorProfileBare: String,
                     colorProfileFullPath: String) = {

    def replace(line: String): String = line.replace("$COLOUR_PROFILE$", colorProfileFullPath)

      Logger.debug(s"Filtering pdf ${baserDir}conf/${pdDefFileBare}")
      val source = Source fromFile baserDir + "conf/" + pdDefFileBare
      val lines = source.getLines
      val result = lines.map(line => replace(line))

      val file = new File(pdDefFileFullPath)
      val bw = new BufferedWriter(new FileWriter(file))

      bw.write(result.mkString("\n"))
      bw.close()
      source.close()

      setUpConfigFile(colorProfileBare, colorProfileFullPath)

  }

  private def setUpConfigFile(sourceFile: String, destinationFile: String): Unit = {
    if (!new File(destinationFile).exists) {
      Logger.debug(s"Byte Copying ${sourceFile} to ${destinationFile}")
      val bytes = reader("/" + sourceFile)
      writer(destinationFile, bytes)
    }
  }

}
