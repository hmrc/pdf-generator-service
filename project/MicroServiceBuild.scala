import sbt._

object MicroServiceBuild extends Build with MicroService {

  val appName = "pdf-generator-service"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val microserviceBootstrapVersion = "1.9.0"
  private val authClientVersion = "2.19.0-play-26"
  private val domainVersion = "5.3.0"
  private val hmrcTestVersion = "3.9.0-play-26"
  private val scalaTestVersion = "2.2.6"
  private val pegdownVersion = "1.6.0"

  private val playReactivemongoVersion = "6.4.0"

  val compile = Seq(
    "org.reactivemongo" %% "play2-reactivemongo"  % "0.18.8-play26",
    "org.jsoup"   %  "jsoup"              % "1.10.2",

    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-26" % "5.7.0",
    "uk.gov.hmrc" %% "domain" % "5.10.0-play-26",
    "io.github.cloudify" %% "spdf" % "1.3.1"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = List()
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % "3.3.3" % scope,
        "org.apache.pdfbox" % "pdfbox" % "2.0.13"
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
