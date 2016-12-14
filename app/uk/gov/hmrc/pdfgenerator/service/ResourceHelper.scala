package uk.gov.hmrc.pdfgenerator.service

import java.io.{BufferedInputStream, BufferedOutputStream, FileOutputStream}

/**
  * Created by peter on 14/12/2016.
  */
object ResourceHelper {

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

}
