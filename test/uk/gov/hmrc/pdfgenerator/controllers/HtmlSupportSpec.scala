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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HtmlSupportSpec extends AnyFlatSpec with Matchers with HtmlSupport {

  val HTML = "<html><body><h1>Some Html</h1></body></html>"

  val CREATE_PDF_A = false

  val HTML_WITH_SCRIPT_TAG = "<html><script>function(){};</script><body><h1>Some Html</h1></body></html>"

  val MORE_HTML_WITH_SCRIPT = """
                                |<H1>Foo Bar</H1>
                                |<script lang='javascript'/>""".stripMargin

  "HtmlSupport" should
    "bind a single non empty form element" in {
    val form = getPdfForm()

    form
      .bind(Map("html" -> HTML, "force-pdfa" -> CREATE_PDF_A.toString))
      .fold(_ => "errors", h => {
        h shouldBe PdfForm(HTML, CREATE_PDF_A)
      })

  }

  it should
    "create a binding error if the html is empty" in {
    val form = getPdfForm()

    form
      .bind(Map("html" -> ""))
      .fold(
        form => {
          form.errors.size == 1
          val errorsSeq = form.errors.map(formError => formError.messages).flatten
          errorsSeq.contains("error.required") shouldBe true
        },
        _ => "html"
      )

  }

  it should
    "create a binding error if the html is not present" in {
    val form = getPdfForm()

    form
      .bind(Map("a" -> "b"))
      .fold(
        form => {
          form.errors.size == 1
          val errorsSeq = form.errors.map(formError => formError.messages).flatten
          errorsSeq.contains("error.required") shouldBe true
        },
        _ => "html"
      )

  }

  it should
    "create a binding error if the html contains a script tag" in {
    val form = getPdfForm()

    form
      .bind(Map("html" -> HTML_WITH_SCRIPT_TAG))
      .fold(
        form => {
          form.errors.size shouldBe 1
          val errorsSeq = form.errors.map(formError => formError.messages).flatten
          errorsSeq.contains(SCRIPT_ERROR_TAG) shouldBe true
        },
        _ => "html"
      )

  }

}
