package uk.gov.hmrc.pdfgenerator.service

import java.io.{File, FileInputStream}
import java.util.{Properties, UUID}

import uk.gov.hmrc.play.http.BadRequestException

import scala.concurrent.Future


object PdfGeneratorService extends PdfGeneratorService

/**
  * Created by habeeb on 10/10/2016.
  *
  * This class is responsible for generating a PDF from a given
  * HTML page
  *
  */
trait PdfGeneratorService {

  val baseDir: String = new File(".").getCanonicalPath + "/"

  def generatePdfFromHtml(html : String, outputFileName : String) : File = {
    import io.github.cloudify.scala.spdf._
    import java.io._

    if(html == null || html.isEmpty){
      Future.failed(throw new BadRequestException("Html must be provided"))
    }

    if(outputFileName == null || outputFileName.isEmpty){
      Future.failed(throw new BadRequestException("OutputFileName must be provided"))
    }

    // Create a new Pdf converter with a custom configuration
    // run `wkhtmltopdf --extended-help` for a full list of options
    val pdf = Pdf(new PdfConfig {
      orientation := Portrait
      pageSize := "A4"
      marginTop := "1in"
      marginBottom := "1in"
      marginLeft := "1in"
      marginRight := "1in"
    })

    val destinationDocument: File = new File(outputFileName)
    pdf.run(html, destinationDocument)

    return destinationDocument
  }

  def convertToPdfA(inputFileName : String, outputFileName : String) : File = {
    import scala.sys.process.Process

    val configFileLocation: String = sys.props.getOrElse("config.location", default = "")

    assertConfigFileExists(configFileLocation)

    val properties = new Properties()
    properties.load(new FileInputStream(configFileLocation))

    val pdfa_defsLocation: String = properties.getProperty("pdfa_defs.location")

    val command: String = "gs -dPDFA=1 -dPDFACompatibilityPolicy=1  -dNOOUTERSAVE -sProcessColorModel=DeviceRGB " +
      "-sDEVICE=pdfwrite -o " + outputFileName + " " + pdfa_defsLocation + "  " + baseDir + inputFileName
    val pb = Process(command)
    val exitCode = pb.!

    return new File(baseDir + outputFileName)
  }

  private def assertConfigFileExists(configLocation : String) = {
    if(configLocation == null || configLocation.isEmpty){
      Future.failed(throw new BadRequestException("config.properties path is not known"))
    }
  }

  def generateCompliantPdfA(html : String, inputFileName : String, outputFileName : String) : File = {
    import scala.sys.process.Process

    val file: File = generatePdfFromHtml(html, inputFileName)

    val pdfA: File = convertToPdfA(inputFileName, outputFileName)

    val deleteCommand: String = "rm -Rf" + " " + baseDir + inputFileName
    val pd = Process(deleteCommand)
    val exitCodeTwo = pd.!

    return pdfA
  }

}
