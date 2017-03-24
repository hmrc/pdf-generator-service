package uk.gov.hmrc.pdfgenerator.service

import java.io.{BufferedWriter, File, FileWriter, IOException}
import java.util.UUID
import javax.inject.Inject

import play.api.{Configuration, Logger}
import javax.inject.Singleton

import scala.io.Source
import scala.util.{Failure, Success, Try}


object PdfGeneratorService {
  @Inject
  val configuration: Configuration = null

  def apply: PdfGeneratorService = new PdfGeneratorService(configuration, ResourceHelper.apply)

}

trait InitHook {
  def init(): Unit
}

@Singleton
class PdfGeneratorService @Inject()(configuration: Configuration, resourceHelper: ResourceHelper) extends InitHook {


  private val PROD_ROOT = "/app/"
  private val CONFIG_KEY = "pdfGeneratorService."

  // From application.conf or environment specific
  private val BASE_DIR_DEV_MODE: Boolean = configuration.getBoolean(CONFIG_KEY + "baseDirDevMode").getOrElse(false)

  def getBaseDir: String = BASE_DIR_DEV_MODE match {
    case true => new File(".").getCanonicalPath + "/"
    case _ => PROD_ROOT
  }

  private val GS_ALIAS: String = configuration.getString(CONFIG_KEY + "gsAlias").getOrElse("/app/bin/gs-920-linux_x86_64")
  private val BASE_DIR: String = configuration.getString(CONFIG_KEY + "baseDir").getOrElse(getBaseDir)
  private val CONF_DIR: String = configuration.getString(CONFIG_KEY + "confDir").getOrElse(getBaseDir)
  private val WK_TO_HTML_EXECUABLE = configuration.getString(CONFIG_KEY + "wkHtmlToPdfExecutable").getOrElse("/app/bin/wkhtmltopdf")
  private val BARE_PS_DEF_FILE: String = configuration.getString(CONFIG_KEY + "psDef").getOrElse("PDFA_def.ps")
  private val ADOBE_COLOR_PROFILE: String = configuration.getString(CONFIG_KEY + "adobeColorProfile").getOrElse("AdobeRGB1998.icc")

  private val PS_DEF_FILE_FULL_PATH: String = CONF_DIR + BARE_PS_DEF_FILE
  private val ADOBE_COLOR_PROFILE_FULL_PATH: String = CONF_DIR + ADOBE_COLOR_PROFILE


  private def logConfig(): Unit = {
    Logger.debug(s"\n\nPROD_ROOT: ${PROD_ROOT} \nCONFIG_KEY: ${CONFIG_KEY} \nBASE_DIR_DEV_MODE: ${BASE_DIR_DEV_MODE} " +
      s"\nGS_ALIAS: ${GS_ALIAS} \nBASE_DIR: ${BASE_DIR} \nCONF_DIR: ${CONF_DIR} \nWK_TO_HTML_EXECUABLE: ${WK_TO_HTML_EXECUABLE} " +
      s"\nPS_DEF: ${BARE_PS_DEF_FILE} \nADOBE_COLOR_PROFILE: ${ADOBE_COLOR_PROFILE} \nPDFA_CONF: ${PS_DEF_FILE_FULL_PATH} \nICC_CONF: " +
      s"${ADOBE_COLOR_PROFILE_FULL_PATH}\n")
  }


  def init(): Unit = {
    Logger.info("Initialising PdfGeneratorService")
    resourceHelper.setUpPsDefFile(BARE_PS_DEF_FILE, PS_DEF_FILE_FULL_PATH, BASE_DIR,
      ADOBE_COLOR_PROFILE, ADOBE_COLOR_PROFILE_FULL_PATH)
  }

  def generateCompliantPdfA(html: String): Try[File] = {

    logConfig()

    val inputFileName: String = UUID.randomUUID.toString + ".pdf"
    val outputFileName: String = UUID.randomUUID.toString + ".pdf"
    Logger.trace(s"generateCompliantPdfA from ${html}")
    Logger.info(s"generateCompliantPdfA inputFileName: ${inputFileName} outputFileName: ${outputFileName}")

    def cleanUpInputFile = {
      val inputFile = new File(BASE_DIR + inputFileName)
      if (inputFile.exists()) {
        inputFile.delete()
      }
    }

    val triedFile = generatePdfFromHtml(html, BASE_DIR + inputFileName)
      .flatMap(_ => convertToPdfA(BASE_DIR + inputFileName, BASE_DIR + outputFileName))

    cleanUpInputFile
    triedFile

  }

  private def generatePdfFromHtml(html: String, inputFileName: String): Try[File] = {
    import java.io._
    import io.github.cloudify.scala.spdf._

    Try {
      val pdf: Pdf = Pdf(WK_TO_HTML_EXECUABLE, new PdfConfig {
        orientation := Portrait
        pageSize := "A4"
        marginTop := "1in"
        marginBottom := "1in"
        marginLeft := "1in"
        marginRight := "1in"
        disableExternalLinks := true
        disableInternalLinks := true
      })

      val outputFile: File = new File(inputFileName)
      val exitCode = pdf.run(html, outputFile)
      checkExitCode(exitCode, WK_TO_HTML_EXECUABLE)
      checkOutputFile(inputFileName, outputFile)
    }
  }

  private def checkExitCode(exitCode: Int, message: String): Unit =
    if (exitCode != 0) throw new IllegalStateException(s"${message} returned an exitCode of ${exitCode}")


  private def checkOutputFile(outputFileName: String, file: File): File = {
    if (!file.exists()) throw new IOException(s"output file: ${outputFileName} does not exist")
    else {
      file.deleteOnExit() // only when the JVM exits added for safety
      file
    }
  }

  private def convertToPdfA(inputFileName: String, outputFileName: String): Try[File] = {
    import scala.sys.process.Process

    val command: String = GS_ALIAS + " -dPDFA=1 -dPDFACompatibilityPolicy=1  -dNOOUTERSAVE -sProcessColorModel=DeviceRGB " +
      "-sDEVICE=pdfwrite -o " + outputFileName + " " + PS_DEF_FILE_FULL_PATH + "  " + inputFileName

    Logger.debug(s"Running: ${command}")

    Try {
      val process = Runtime.getRuntime().exec(command)
      val exitCode = process.waitFor()
      val file = new File(outputFileName)
      checkExitCode(exitCode, command)
      checkOutputFile(outputFileName, file)
    }

  }

  /**
    * called once by the Guice Play framework as this class is a Singleton
    *
    */
  init()
}


