import com.google.inject.AbstractModule
import uk.gov.hmrc.pdfgenerator.service.PdfGeneratorService

/**
  * This class is used to hook into the Guice Application Lifecycle
  */
class Module extends AbstractModule {

  override def configure(): Unit =
    bind(classOf[PdfGeneratorService]).asEagerSingleton()
}
