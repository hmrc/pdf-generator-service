/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.pdfgenerator.utils

import java.io.{ByteArrayInputStream, File, InputStream}
import java.net.URL

import scala.annotation.implicitNotFound
import scala.sys.process.ProcessBuilder
import scala.xml.Elem

/**
  * Type class that describes the kind of source documents we can read the
  * input HTML from.
  */
@implicitNotFound(msg = "Cannot find SourceDocumentLike type class for ${A}")
trait SourceDocumentLike[-A] {

  /**
    * The source parameter to provide to `wkhtmltopdf`
    * Defaults to read from STDIN.
    */
  def commandParameter(sourceDocument: A): String = "-"

  /**
    * Source the input of the process from this sourceDocument
    * Defaults to passthrough.
    */
  def sourceFrom(sourceDocument: A)(process: ProcessBuilder): ProcessBuilder =
    process

}

object SourceDocumentLike {

  /**
    * Pipes the InputStream into the process STDIN
    */
  implicit object InputStreamSourceDocument extends SourceDocumentLike[InputStream] {

    override def sourceFrom(sourceDocument: InputStream)(process: ProcessBuilder): ProcessBuilder =
      process #< sourceDocument

  }

  /**
    * Sets the file absolute path as the input parameter
    */
  implicit object FileSourceDocument extends SourceDocumentLike[File] {

    override def commandParameter(sourceDocument: File): String =
      sourceDocument.getAbsolutePath

  }

  /**
    * Pipes a UTF-8 string into the process STDIN
    */
  implicit object StringSourceDocument extends SourceDocumentLike[String] {

    override def sourceFrom(sourceDocument: String)(process: ProcessBuilder) =
      process #< toInputStream(sourceDocument)

    private def toInputStream(sourceDocument: String): ByteArrayInputStream =
      new ByteArrayInputStream(sourceDocument.getBytes("UTF-8"))

  }

  /**
    * Sets the URL as the input parameter
    */
  implicit object URLSourceDocument extends SourceDocumentLike[URL] {

    override def commandParameter(sourceDocument: URL) = sourceDocument.getProtocol match {
      case "https" | "http" | "file" => sourceDocument.toString
      case _                         => throw new UnsupportedProtocolException(sourceDocument)
    }

  }

  /**
    * Sets the XML node as the input parameter
    */
  implicit object XmlSourceDocument extends SourceDocumentLike[Elem] {

    override def sourceFrom(sourceDocument: Elem)(process: ProcessBuilder) =
      process #< toInputStream(sourceDocument)

    private def toInputStream(sourceDocument: Elem): ByteArrayInputStream =
      new ByteArrayInputStream(sourceDocument.toString().getBytes("UTF-8"))

  }

}
