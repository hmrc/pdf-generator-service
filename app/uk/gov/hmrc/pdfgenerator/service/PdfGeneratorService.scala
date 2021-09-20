/*
 * Copyright 2021 HM Revenue & Customs
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

import java.io.{File, IOException}
import java.net.URL
import java.util.UUID

import javax.inject.{Inject, Singleton}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.{Configuration, Environment, Logging, Mode}

import scala.collection.JavaConverters._
import scala.sys.process._
import scala.util.{Failure, Success, Try}

@Singleton
class PdfGeneratorService @Inject()(
  configuration: Configuration,
  resourceHelper: ResourceHelper,
  environment: Environment)
    extends Logging {

  val EMPTY_INDICATOR = "EMPTY_FOR_PROD_DEFAULT"
  val PROD_ROOT = "/app/"
  val CONFIG_KEY = "pdfGeneratorService."

  // From application.conf or environment specific
  val BASE_DIR_DEV_MODE: Boolean = configuration.getBoolean(CONFIG_KEY + "baseDirDevMode").getOrElse(false)

  def getBaseDir: String =
    if (BASE_DIR_DEV_MODE) {
      new File(".").getCanonicalPath + "/"
    } else {
      PROD_ROOT
    }

  private def default(configuration: Configuration, key: String, productionDefault: String): String =
    Try[String] {
      val value: String = configuration.getOptional[String](CONFIG_KEY + key).getOrElse(productionDefault)
      environment.mode match {
        case Mode.Prod => productionDefault
        case Mode.Test => productionDefault
        case Mode.Dev  => value
      }
    } match {
      case Success(value) => value
      case Failure(_) =>
        Logger.error(s"Failed to find a value for $key defaulting to $productionDefault")
        productionDefault
    }

  private def getEnvironmentPath(file: String) =
    environment.mode match {
      case Mode.Prod => s"bin/$file"
      case Mode.Test => s"target/extra/bin/$file"
      case Mode.Dev  => s"target/extra/bin/$file"
    }

  val GS_ALIAS: String = {
    default(configuration, "gsAlias", getBaseDir + getEnvironmentPath("gs-920-linux_x86_64"))
  }
  val BASE_DIR: String = default(configuration, "baseDir", getBaseDir)
  val CONF_DIR: String = default(configuration, "confDir", getBaseDir)
  val WK_TO_HTML_EXECUTABLE: String = {
    default(configuration, "wkHtmlToPdfExecutable", getBaseDir + getEnvironmentPath("wkhtmltopdf"))
  }
  val BARE_PS_DEF_FILE: String = default(configuration, "psDef", "PDFA_def.ps")
  val ADOBE_COLOR_PROFILE: String = default(configuration, "adobeColorProfile", "AdobeRGB1998.icc")
  val PS_DEF_FILE_FULL_PATH: String = getBaseDir + BARE_PS_DEF_FILE
  val ADOBE_COLOR_PROFILE_FULL_PATH: String = CONF_DIR + ADOBE_COLOR_PROFILE

  private def logConfig(): Unit = {
    val checkGSfile = new File(GS_ALIAS)
    logger.debug(
      s"\n absolutePath: ${checkGSfile.getAbsolutePath} \n exists: ${checkGSfile.exists()} \n canExecute: ${checkGSfile.canExecute}")

    val checkWkfile = new File(WK_TO_HTML_EXECUTABLE)
    logger.debug(
      s"\n absolutePath: ${checkWkfile.getAbsolutePath} \n exists: ${checkWkfile.exists()} \n canExecute: ${checkWkfile.canExecute}")

    logger.debug(s"\n\nPROD_ROOT: $PROD_ROOT \nCONFIG_KEY: $CONFIG_KEY \nBASE_DIR_DEV_MODE: $BASE_DIR_DEV_MODE " +
      s"\nGS_ALIAS: $GS_ALIAS \nBASE_DIR: $BASE_DIR \nCONF_DIR: $CONF_DIR \nWK_TO_HTML_EXECUABLE: $WK_TO_HTML_EXECUTABLE " +
      s"\nPS_DEF: $BARE_PS_DEF_FILE \nADOBE_COLOR_PROFILE: $ADOBE_COLOR_PROFILE \nPDFA_CONF: $PS_DEF_FILE_FULL_PATH \nICC_CONF: " +
      s"$ADOBE_COLOR_PROFILE_FULL_PATH")
  }

  def init(): Unit = {
    logger.info("Initialising PdfGeneratorService")

    resourceHelper.setupExecutableSupportFiles(
      BARE_PS_DEF_FILE,
      PS_DEF_FILE_FULL_PATH,
      BASE_DIR,
      ADOBE_COLOR_PROFILE,
      ADOBE_COLOR_PROFILE_FULL_PATH)
  }

  def generatePdf(html: String, forcePdfA: Boolean): Try[File] = {
    logConfig()
    val inputFileName: String = UUID.randomUUID.toString + ".pdf"
    val outputFileName: String = UUID.randomUUID.toString + ".pdf"
    logger.trace(s"generatePdf from $html")
    val linksDisabled = if (forcePdfA) true else getLinksDisabled(html)

    try {

      val triedFile = generatePdfFromHtml(html, BASE_DIR + inputFileName, linksDisabled)
      if (linksDisabled) {
        triedFile.flatMap(_ => convertToPdfA(getBaseDir + inputFileName, getBaseDir + outputFileName))
      } else {
        logger.warn("*** Generated PDF will not be PDF/A compliant as it contains valid gov.uk links ***")
        triedFile
      }
    } finally { if (!linksDisabled) deleteFile(BASE_DIR + inputFileName) }
  }

  private def deleteFile(fileName: String): AnyVal = {
    val file = new File(BASE_DIR + fileName)
    if (file.exists()) {
      file.delete()
    }
  }

  /**
    * Returns false if hyperlinks in source HTML are valid and true if links are invalid
    * @param html source HTML
    * @return true or false
    */
  def getLinksDisabled(html: String): Boolean = {
    val startTime = System.currentTimeMillis()
    val links = extractLinksFromHtml(html)
    logger.trace("Checking document for links took " + (System.currentTimeMillis() - startTime) + " milliseconds")
    if (onlyContainsValidLinks(links)) false else true
  }

  private def extractLinksFromHtml(html: String): List[String] = {
    val doc: Document = Jsoup.parse(html)
    doc.getElementsByTag("a").asScala.map(link => link.attr("href")).toList
  }

  private def parseUrl(url: String): Try[URL] = Try(new URL(url))

  private def validateDomain(domain: String): Boolean =
    if (domain.endsWith("gov.uk") || domain.endsWith("localhost")) true
    else {
      logger.warn(s"External link to $domain detected. All links in document will be disabled")
      false
    }

  private def validateUrl(linkText: String): Boolean =
    parseUrl(linkText) match {
      case Success(url) => validateDomain(url.getHost)
      case Failure(e) =>
        logger.error(s"Unable to parse link $linkText text as URL due to " + e.getMessage)
        false
    }

  private def onlyContainsValidLinks(links: List[String]): Boolean =
    links.forall(link => validateUrl(link))

  private def generatePdfFromHtml(html: String, inputFileName: String, linksDisabled: Boolean): Try[File] = {
    import java.io._

    import io.github.cloudify.scala.spdf._

    Try {
      val pdf: Pdf = Pdf(
        WK_TO_HTML_EXECUTABLE,
        new PdfConfig {
          orientation := Portrait
          pageSize := "A4"
          marginTop := "1in"
          marginBottom := "1in"
          marginLeft := "1in"
          marginRight := "1in"
          disableExternalLinks := linksDisabled
          disableInternalLinks := true
        }
      )

      val outputFile: File = new File(inputFileName)
      val exitCode = pdf.run(html, outputFile)
      checkExitCode(exitCode, WK_TO_HTML_EXECUTABLE)
      checkOutputFile(inputFileName, outputFile)
    }
  }

  private def checkExitCode(exitCode: Int, message: String): Unit =
    if (exitCode != 0) throw new IllegalStateException(s"$message returned an exitCode of $exitCode")

  private def checkOutputFile(outputFileName: String, file: File): File =
    if (!file.exists()) throw new IOException(s"output file: $outputFileName does not exist")
    else {
      file.deleteOnExit() // only when the JVM exits added for safety
      file
    }

  private def convertToPdfA(inputFileName: String, outputFileName: String): Try[File] = {

    logger.info(s"generateCompliantPdfA inputFileName: $inputFileName outputFileName: $outputFileName")

    val commands: Seq[String] = List(
      GS_ALIAS,
      "-dPDFA=1",
      "-dPDFACompatibilityPolicy=1",
      "-dNOOUTERSAVE",
      "-dNOSAFER",
      "-sProcessColorModel=DeviceRGB",
      "-sDEVICE=pdfwrite",
      "-o",
      outputFileName,
      PS_DEF_FILE_FULL_PATH,
      inputFileName
    )

    logger.debug(s"Running: ${commands.mkString(" ")}")

    Try {
      val exitCode = Process(commands).!
      val file = new File(outputFileName)
      checkExitCode(exitCode, commands.mkString(" "))
      checkOutputFile(outputFileName, file)
    }

  }

  /**
    * called once by the Guice Play framework as this class is a Singleton
    *
    */
  init()

}
