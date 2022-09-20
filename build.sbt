import sbt.Keys._
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport.scalafmtOnCompile
import uk.gov.hmrc._
import DefaultBuildSettings.{addTestReportOption, defaultSettings}
import sbt.io.Path.contentOf
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.SbtArtifactory

val appName: String = "pdf-generator-service"
val silencerVersion = "1.7.8"

lazy val downloadBinaryDependencies = taskKey[Unit]("downloadBinaryDependencies")
lazy val plugins: Seq[Plugins] = Seq(
  play.sbt.PlayScala,
  SbtAutoBuildPlugin,
  SbtGitVersioning,
  SbtDistributablesPlugin,
  SbtArtifactory
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins: _*)
  .settings(
    majorVersion := 1,
    defaultSettings(),
    scalaVersion := "2.12.13",
    libraryDependencies ++= AppDependencies.all,
    retrieveManaged := true,
    scalafmtOnCompile := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "it")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / parallelExecution := false
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )
  .settings(
    executableFilesInTar := Seq("wkhtmltopdf", "gs-920-linux_x86_64")
  )
  .settings(
    scalacOptions ++= Seq(
      "-feature",
      "-Xfatal-warnings",
      "-P:silencer:pathFilters=routes,silencer:pathFilters=twirl"
    ),
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
  )
  .settings(
    downloadBinaryDependencies := {

      import scala.concurrent.Await
      import scala.concurrent.ExecutionContext.Implicits.global
      import scala.concurrent.duration.DurationLong
      import scala.sys.process._

      val tempDir = "/tmp/"
      val extraDir = target.value / "extra"
      val binDir: File = extraDir / "bin"

      val githubToken = sys.env.getOrElse("GITHUB_API_TOKEN", sys.error("env var GITHUB_API_TOKEN is required"))

      def download(url: String, target: File) = {
        val req = dispatch.url(url).GET.addHeader("Authorization", s"Token $githubToken")
        Await.result(
          dispatch.Http
            .default(req)
            .map { res =>
              if (res.getStatusCode != 200)
                sys.error(s"Failed to download $url statusCode ${res.getStatusCode}")
              java.nio.file.Files.write(target.toPath, res.getResponseBodyAsBytes)
            },
          1.minute
        )
      }

      Process(Seq("mkdir", "-p", extraDir.getAbsolutePath)).!!
      Process(Seq("mkdir", "-p", binDir.getAbsolutePath)).!!

      println("Downloading GhostScript...")
      val ghostscript = new File(tempDir, "ghostscript.tgz")
      download(
        s"https://raw.githubusercontent.com/hmrc/pdf-generator-service-dependencies/main/ghostscript-9.20-linux-x86_64.tgz",
        ghostscript)
      println(s"Extracting GhostScript to ${binDir.getAbsolutePath}/gs-920-linux_x86_64")
      Process(Seq("tar", "zxf", tempDir + ghostscript.getName, "-C", binDir.getAbsolutePath, "--strip-components", "1")).!!
      Process(Seq("chmod", "+x", s"${binDir.getAbsolutePath}/gs-920-linux_x86_64")).!!
      ghostscript.delete()

      println("Downloading wkhtmltopdf...")
      val wkhtmltox = new File(tempDir, "wkhtmltopdf.tgz")
      download(
        "https://raw.githubusercontent.com/hmrc/pdf-generator-service-dependencies/main/wkhtmltox-0.12.4_linux-generic-amd64.tar.xz",
        wkhtmltox)
      println(s"Extracting wkhtmltopdf to ${extraDir.getAbsolutePath}/bin/wkhtmltopdf")
      Process(Seq("tar", "xJf", tempDir + wkhtmltox.getName, "-C", extraDir.getAbsolutePath, "--strip-components", "1")).!!
      Process(Seq("chmod", "+x", s"${extraDir.getAbsolutePath}/bin/wkhtmltopdf")).!!
      wkhtmltox.delete()
    },
    Universal / mappings ++= contentOf(target.value / "extra"),
    update := (update dependsOn downloadBinaryDependencies).value
  )
