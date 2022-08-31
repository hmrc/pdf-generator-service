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

package uk.gov.hmrc.pdfgenerator.controllers
import play.api.data.Forms.{boolean, default, mapping, text}
import play.api.data.validation._
import play.api.data.{Form, Mapping}

import scala.util.matching.Regex

/**
  * Created by peter on 22/03/2017.
  */
trait HtmlSupport {

  private val noScriptRegex: Regex = ".?<script.*".r

  val SCRIPT_ERROR_TAG = "error.noScriptTagsAllowed"

  def noScriptTags(errorMessage: String = SCRIPT_ERROR_TAG): Constraint[String] = Constraint[String](SCRIPT_ERROR_TAG) {
    html =>
      val maybeInvalid: Option[Invalid] = noScriptRegex
        .findFirstIn(html)
        .map(_ => Invalid(ValidationError(errorMessage)))

      maybeInvalid.getOrElse(Valid)
  }

  val noScriptTags: Constraint[String] = noScriptTags()

  val html: Mapping[String] = text verifying (Constraints.nonEmpty, noScriptTags)

  case class PdfForm(html: String, forcePdfA: Boolean)

  def getPdfForm() = Form(
    mapping(
      "html"       -> html,
      "force-pdfa" -> default(boolean, true)
    )(PdfForm.apply)(PdfForm.unapply)
  )
}
