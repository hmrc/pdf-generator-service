package uk.gov.hmrc.pdfgenerator.service

import java.io.{File, FileInputStream}
import java.util.{Properties, UUID}

import uk.gov.hmrc.play.http.BadRequestException

import scala.concurrent.Future
import scala.io.Source

import scala.sys.process._


object PdfGeneratorService extends PdfGeneratorService


/**
  * Created by habeeb on 10/10/2016.
  *
  * This class is responsible for generating a PDF from a given
  * HTML page
  *
  */
trait PdfGeneratorService {

  def getFileFromClasspath(name: String): File = {
    //val source = Source.fromURL(getClass.getResource("/" + name)).
    val path = (getClass.getResource("/" + name)).getPath
    //val reader = new BufferedReader(new InputStreamReader(istream));
    new File(path)
  }

  private val PDFAdef_psFile: File = getFileFromClasspath("PDFA_def.ps")
  val baseDir: String = "/app/"

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

    class myPdfConfig extends PdfConfig {

      def findExecutable: Option[String] = try {
      Option("which wkhtmltopdf".!!.trim).filter(_.nonEmpty)
      } catch {
        case _: RuntimeException => Option("/app/bin/wkhtmltopdf")
      }
    }


    val pdf = Pdf( "/app/bin/wkhtmltopdf", new PdfConfig {
      orientation := Portrait
      pageSize := "A4"
      marginTop := "1in"
      marginBottom := "1in"
      marginLeft := "1in"
      marginRight := "1in"
    })
//
//    val pdf = Pdf( new myPdfConfig {
//            orientation := Portrait
//            pageSize := "A4"
//            marginTop := "1in"
//            marginBottom := "1in"
//            marginLeft := "1in"
//            marginRight := "1in"
//    })

    val destinationDocument: File = new File("/app/" + outputFileName)
    pdf.run(html, destinationDocument)

    return destinationDocument
  }

  def convertToPdfA(inputFileName : String, outputFileName : String) : File = {
    import scala.sys.process.Process


    val pdfa_defsLocation: String = sys.props.getOrElse("pdfa_defs.location", default = "")

    val command: String = "gs -dPDFA=1 -dPDFACompatibilityPolicy=1  -dNOOUTERSAVE -sProcessColorModel=DeviceRGB -sDEVICE=pdfwrite -o " + outputFileName + " " + PDFAdef_psFile + " " + baseDir + inputFileName
    //val command: String = "gs -dPDFA=1 -dPDFACompatibilityPolicy=1  -dNOOUTERSAVE -sProcessColorModel=DeviceRGB -sDEVICE=pdfwrite -o PDFAcompliant.pdf" + " " + pdfa_defsLocation2 + " " + "non-compliant.pdf"
    val pb = Process(command)
    val exitCode = pb.!

    return new File(baseDir + outputFileName)
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
