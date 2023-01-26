package uk.gov.hmrc.pdfgenerator.utils

sealed trait PageOrientation {
  val value: String
}

object Landscape extends PageOrientation {
  override val value = "Landscape"
}

object Portrait extends PageOrientation {
  override val value = "Portrait"
}
