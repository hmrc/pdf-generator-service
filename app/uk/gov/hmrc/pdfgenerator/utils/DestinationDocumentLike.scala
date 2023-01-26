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

import java.io.{File, OutputStream}

import scala.annotation.implicitNotFound
import scala.sys.process.ProcessBuilder

/**
  * Type class that describes the kind of destination type we can write the
  * resulting PDF documents to.
  */
@implicitNotFound(msg = "Cannot find DestinationDocumentLike type class for ${A}")
trait DestinationDocumentLike[-A] {

  /**
    * The destination argument to supply to `wkhtmltopdf`
    */
  def commandParameter(destinationDocument: A): String = "-"

  /**
    * Sink the process output into this document
    */
  def sinkTo(destinationDocument: A)(process: ProcessBuilder): ProcessBuilder =
    process

}

object DestinationDocumentLike {

  implicit object FileDestinationDocument extends DestinationDocumentLike[File] {

    override def commandParameter(destinationDocument: File): String =
      destinationDocument.getAbsolutePath

  }

  implicit object OutputStreamDestinationDocument extends DestinationDocumentLike[OutputStream] {

    override def sinkTo(destinationDocument: OutputStream)(process: ProcessBuilder) =
      process #> destinationDocument

  }

}
