package uk.gov.hmrc.pdfgenerator.service

import java.io.{File, IOException}
import java.util.UUID

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import uk.gov.hmrc.pdfgenerator.metrics.PdfGeneratorMetric

import scala.sys.process._
import scala.util.{Failure, Success, Try}


trait InitHook {
  def init(): Unit
}

@Singleton
class PdfGeneratorService @Inject()(configuration: Configuration, resourceHelper: ResourceHelper) extends InitHook {

  val EMPTY_INDICATOR = "EMPTY_FOR_PROD_DEFAULT"
  val PROD_ROOT = "/app/"
  val CONFIG_KEY = "pdfGeneratorService."

  // From application.conf or environment specific
  val RUN_MODE = configuration.getString(CONFIG_KEY + "runmode").getOrElse("prod").toLowerCase

  def getBaseDir: String = RUN_MODE match {
      case "prod" => PROD_ROOT
      case "test" => new File(".").getCanonicalPath + "/"
      case "dev"  => new File(".").getCanonicalPath + "/"
    }

  private def default(configuration: Configuration, key: String, productionDefault: String): String = {
    Try[String] {
      val value = configuration.getString(CONFIG_KEY + key).getOrElse(productionDefault)
      RUN_MODE match {
        case "prod" => productionDefault
        case "test" => productionDefault
        case "dev"  => value
      }
    } match {
      case Success(value) => value
      case Failure(_) => {
        Logger.error(s"Failed to find a value for ${key} defaulting to ${productionDefault}")
        productionDefault
      }
    }
  }

  private def getEnvironmentPath(file: String) = {
    RUN_MODE match {
      case "prod" => s"bin/$file"
      case "test" => s"target/extra/bin/$file"
      case "dev"  => s"target/extra/bin/$file"
    }
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
    Logger.debug(s"\n absolutePath: ${checkGSfile.getAbsolutePath} \n exists: ${checkGSfile.exists()} \n canExecute: ${checkGSfile.canExecute}")

    val checkWkfile = new File(WK_TO_HTML_EXECUTABLE)
    Logger.debug(s"\n absolutePath: ${checkWkfile.getAbsolutePath} \n exists: ${checkWkfile.exists()} \n canExecute: ${checkWkfile.canExecute}")

    Logger.debug(s"\n\nPROD_ROOT: ${PROD_ROOT} \nCONFIG_KEY: ${CONFIG_KEY} \nRUN_MODE: ${RUN_MODE} " +
      s"\nGS_ALIAS: ${GS_ALIAS} \nBASE_DIR: ${BASE_DIR} \nCONF_DIR: ${CONF_DIR} \nWK_TO_HTML_EXECUABLE: ${WK_TO_HTML_EXECUTABLE} " +
      s"\nPS_DEF: ${BARE_PS_DEF_FILE} \nADOBE_COLOR_PROFILE: ${ADOBE_COLOR_PROFILE} \nPDFA_CONF: ${PS_DEF_FILE_FULL_PATH} \nICC_CONF: " +
      s"${ADOBE_COLOR_PROFILE_FULL_PATH}\n Diskspace: ${PdfGeneratorMetric.gauge.getValue}Mb")
  }


  def init(): Unit = {
    Logger.info("Initialising PdfGeneratorService")

    resourceHelper.setupExecutableSupportFiles(BARE_PS_DEF_FILE, PS_DEF_FILE_FULL_PATH, BASE_DIR,
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
      .flatMap(_ => convertToPdfA(getBaseDir + inputFileName, getBaseDir + outputFileName))

    cleanUpInputFile
    triedFile

  }

  import org.jsoup.Jsoup
  import org.jsoup.nodes.Document

  import scala.collection.JavaConverters._

  def extractLinksFromHtml(html: String): List[String] = {
    val doc: Document = Jsoup.parse(html)
    Option(doc.getElementsByTag("a")) match {
      case Some(links) => links.asScala.map(link => link.attr("href")).toList
      case None        => List.empty
    }
  }

  import scala.util.matching.Regex

  def validLinkChecker(links: List[String]):Boolean = {
     links.forall(link => regExprChecker(link))
  }

  def regExprChecker(link: String): Boolean = {
    val validGovUkChecker: Regex = "https://[a-z.]*gov.uk[/a-z0-9-]*".r
    link match {
      case validGovUkChecker() => true
      case _                   => false
    }
  }

  def externalLinkEnabler(html: String): Boolean = {
    validLinkChecker(extractLinksFromHtml(html))
  }

  private def generatePdfFromHtml(html: String, inputFileName: String): Try[File] = {
    import java.io._

    import io.github.cloudify.scala.spdf._

    Try {
      val pdf: Pdf = Pdf(WK_TO_HTML_EXECUTABLE, new PdfConfig {
        orientation := Portrait
        pageSize := "A4"
        marginTop := "1in"
        marginBottom := "1in"
        marginLeft := "1in"
        marginRight := "1in"
        disableExternalLinks := externalLinkEnabler(html)
        disableInternalLinks := true
      })

      val outputFile: File = new File(inputFileName)
      val exitCode = pdf.run(html, outputFile)
      checkExitCode(exitCode, WK_TO_HTML_EXECUTABLE)
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

    val commands: Seq[String] = List(GS_ALIAS, "-dPDFA=1", "-dPDFACompatibilityPolicy=1", "-dNOOUTERSAVE",
                                    "-sProcessColorModel=DeviceRGB", "-sDEVICE=pdfwrite", "-o", outputFileName,
                                    PS_DEF_FILE_FULL_PATH, inputFileName)

    Logger.debug(s"Running: ${commands.mkString(" ")}")

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


