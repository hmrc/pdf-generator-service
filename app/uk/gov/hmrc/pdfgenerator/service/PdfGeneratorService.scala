package uk.gov.hmrc.pdfgenerator.service

import java.io.{BufferedWriter, File, FileWriter}
import java.util.UUID
import javax.inject.Inject

import play.api.{Configuration, Logger}

import scala.io._


object PdfGeneratorService {
  @Inject
  val configuration: Configuration = null
  def apply: PdfGeneratorService = new PdfGeneratorService(configuration)
}

class PdfGeneratorService @Inject()(configuration: Configuration) {

  private val PROD_ROOT = "/app/"
  private val CONFIG_KEY = "pdfGeneratorService."

  // From application.conf or environment specific
  private val BASE_DIR_DEV_MODE: Boolean = configuration.getBoolean(CONFIG_KEY + "baseDirDevMode").getOrElse(false)

  def getBaseDir: String = BASE_DIR_DEV_MODE match {
    case true => new File(".").getCanonicalPath + "/"
    case _ => PROD_ROOT
  }

  private val GS_ALIAS: String  = configuration.getString(CONFIG_KEY + "gsAlias").getOrElse("/app/bin/gs-920-linux_x86_64")
  private val BASE_DIR: String = configuration.getString(CONFIG_KEY + "baseDir").getOrElse(getBaseDir)
  private val CONF_DIR: String  = configuration.getString(CONFIG_KEY + "confDir").getOrElse(getBaseDir)
  private val WK_TO_HTML_EXECUABLE = configuration.getString(CONFIG_KEY + "wkHtmlToPdfExecutable").getOrElse("/app/bin/wkhtmltopdf")
  private val BARE_PS_DEF_FILE: String  = configuration.getString(CONFIG_KEY + "psDef").getOrElse("PDFA_def.ps")
  private val ADOBE_COLOR_PROFILE: String  = configuration.getString(CONFIG_KEY + "adobeColorProfile").getOrElse("AdobeRGB1998.icc")

  private val PS_DEF_FILE_FULL_PATH: String  = CONF_DIR + BARE_PS_DEF_FILE
  private val ADOBE_COLOR_PROFILE_FULL_PATH: String  = CONF_DIR + ADOBE_COLOR_PROFILE


  def logConfig(): Unit = {
    Logger.debug(s"\nPROD_ROOT: ${PROD_ROOT} \nCONFIG_KEY: ${CONFIG_KEY} \nBASE_DIR_DEV_MODE: ${BASE_DIR_DEV_MODE} " +
      s"\nGS_ALIAS: ${GS_ALIAS} \nBASE_DIR: ${BASE_DIR} \nCONF_DIR: ${CONF_DIR} \nWK_TO_HTML_EXECUABLE: ${WK_TO_HTML_EXECUABLE} " +
      s"\nPS_DEF: ${BARE_PS_DEF_FILE} \nADOBE_COLOR_PROFILE: ${ADOBE_COLOR_PROFILE} \nPDFA_CONF: ${PS_DEF_FILE_FULL_PATH} \nICC_CONF: " +
      s"${ADOBE_COLOR_PROFILE_FULL_PATH}")
  }


  private def generatePdfFromHtml(html: String, inputFileName: String): File = {
    import java.io._
    import io.github.cloudify.scala.spdf._


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
    outputFile.deleteOnExit() // only when the JVM exits added for safety
    pdf.run(html, outputFile)

    outputFile
  }


  def setUpPsDefFile(pdDefFileBare: String, pdDefFileFullPath: String) = {

    def replace(line: String): String = line.replace("$COLOUR_PROFILE$", ADOBE_COLOR_PROFILE_FULL_PATH)

    if(!new File(pdDefFileFullPath).exists) {
      Logger.debug(s"Filtering pdf ${BASE_DIR}conf/${pdDefFileBare}")
      val source = Source .fromFile(BASE_DIR + "conf/" + pdDefFileBare)
      val lines = source.getLines
      val result = lines.map(line => replace(line))

      val file = new File(pdDefFileFullPath)
      val bw = new BufferedWriter(new FileWriter(file))

      bw.write(result.mkString("\n"))
      bw.close()
      source.close()

    } else {
      setUpConfigFile(pdDefFileBare, pdDefFileFullPath)
    }
  }


  private def convertToPdfA(inputFileName: String, outputFileName: String): File = {
    import scala.sys.process.Process

    setUpPsDefFile(BARE_PS_DEF_FILE, PS_DEF_FILE_FULL_PATH)
    setUpConfigFile(ADOBE_COLOR_PROFILE, ADOBE_COLOR_PROFILE_FULL_PATH)

    val command: String = GS_ALIAS + " -dPDFA=1 -dPDFACompatibilityPolicy=1  -dNOOUTERSAVE -sProcessColorModel=DeviceRGB " +
      "-sDEVICE=pdfwrite -o " + outputFileName + " " + PS_DEF_FILE_FULL_PATH + "  " + inputFileName

    Logger.info(s"Running: ${command}")
    val pb = Process(command)
    val exitCode = pb.!

    val file = new File(outputFileName)
    file.deleteOnExit()
    file
  }



  def generateCompliantPdfA(html: String): File = {

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

    try {
      generatePdfFromHtml(html, BASE_DIR + inputFileName)
      convertToPdfA(BASE_DIR + inputFileName, BASE_DIR + outputFileName)
    } finally {
      cleanUpInputFile
    }
  }

  def setUpConfigFile(fileName: String, configPath: String): Unit = {
    if (!new File(configPath).exists) {
      val bytes = ResourceHelper.reader("/" + fileName)
      ResourceHelper.writer(configPath, bytes)
    }
  }

}