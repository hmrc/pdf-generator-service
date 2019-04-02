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

  def noScriptTags(errorMessage: String = SCRIPT_ERROR_TAG): Constraint[String] = Constraint[String](SCRIPT_ERROR_TAG) { html =>
    val maybeInvalid: Option[Invalid] = noScriptRegex.findFirstIn(html)
      .map(_ => Invalid(ValidationError(errorMessage)))

    maybeInvalid.getOrElse(Valid)
  }


  val noScriptTags: Constraint[String] = noScriptTags()

  val html: Mapping[String] = text verifying (Constraints.nonEmpty , noScriptTags)

  case class PdfForm(html: String, createPdfA: Boolean)

  def getPdfForm() = Form(
      mapping(
        "html" -> html,
        "create-pdfa" -> default(boolean, false)
      )(PdfForm.apply)(PdfForm.unapply)
    )
}
