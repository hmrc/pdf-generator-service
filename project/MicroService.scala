import play.routes.compiler.StaticRoutesGenerator
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import com.typesafe.sbt.SbtNativePackager._

import sbt.ModuleID
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport.scalafmtOnCompile
import scala.concurrent.Await

trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings.{defaultSettings, scalaSettings, addTestReportOption}
  import TestPhases._
  import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
  import _root_.play.sbt.routes.RoutesKeys.routesGenerator
  import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion
  import uk.gov.hmrc.SbtAutoBuildPlugin
  import uk.gov.hmrc.versioning.SbtGitVersioning
  import uk.gov.hmrc.SbtArtifactory
  import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._

  val appName: String

  lazy val appDependencies : Seq[ModuleID] = List()
  lazy val plugins : Seq[Plugins] = Seq.empty
  lazy val playSettings : Seq[Setting[_]] = Seq.empty

  lazy val downloadBinaryDependenciesTask = taskKey[Unit]("Downloads ghostscript and wkhtmltox")

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(Seq(_root_.play.sbt.PlayScala,SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory) ++ plugins : _*)
    .settings(playSettings : _*)
    .settings(scalaSettings: _*)
    .settings(publishingSettings: _*)
    .settings(majorVersion := 1)
    .settings(defaultSettings(): _*)
    .settings(
      libraryDependencies ++= appDependencies,
      retrieveManaged := true,
      scalafmtOnCompile := true,
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    )
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(
      downloadBinaryDependenciesTask := {

        import scala.sys.process._
        val tempDir = "/tmp/"
        val extraDir = target.value / "extra"
        val binDir: File = extraDir / "bin"

        val githubToken = sys.env.getOrElse("GITHUB_API_TOKEN", sys.error("env var GITHUB_API_TOKEN is required"))

        extraDir.mkdir()
        binDir.mkdir()
        println(s"Made dir: ${binDir} (${binDir.getAbsolutePath})")
        println("\npwd: ")
        "pwd".!
        println(s"""\nls -la "${binDir.getAbsolutePath}":""")
        Process(Seq("ls", "-la", binDir.getAbsolutePath)).!

        println(s"Try again..")
        Process(Seq("mkdir", "-p", binDir.getAbsolutePath)).!
        println(s"""\nls -la "${binDir.getAbsolutePath}":""")
        Process(Seq("ls", "-la", binDir.getAbsolutePath)).!

        import scala.concurrent.ExecutionContext.Implicits.global

        def download(url: String, target: File) = {
          import scala.concurrent.duration.DurationLong
          val req = dispatch.url(url).GET.addHeader("Authorization", s"Token $githubToken")
          Await.result(
            dispatch.Http(req)
              .map { res =>
                if (res.getStatusCode != 200)
                  sys.error(s"Failed to download $url statusCode ${res.getStatusCode}")
                java.nio.file.Files.write(target.toPath, res.getResponseBodyAsBytes)
              },
            1.minute
          )
        }

        val ghostscript = new File(tempDir, "ghostscript.tgz")
        download(s"https://raw.githubusercontent.com/hmrc/pdf-generator-service-dependencies/master/ghostscript-9.20-linux-x86_64.tgz", ghostscript)
        Process(Seq("tar", "zxf", tempDir + ghostscript.getName, "-C", binDir.getAbsolutePath, "--strip-components", "1")).!!
        Process(Seq("chmod", "+x", s"${binDir.getAbsolutePath}/gs-920-linux_x86_64")).!!
        ghostscript.delete()

        val wkhtmltox = new File(tempDir, "wkhtmltopdf.tgz")
        download("https://raw.githubusercontent.com/hmrc/pdf-generator-service-dependencies/master/wkhtmltox-0.12.4_linux-generic-amd64.tar.xz", wkhtmltox)
        Process(Seq("tar", "xJf", tempDir + wkhtmltox.getName, "-C", extraDir.getAbsolutePath, "--strip-components", "1")).!!
        Process(Seq("chmod", "+x", s"${extraDir.getAbsolutePath}/bin/wkhtmltopdf")).!!
        wkhtmltox.delete()
      },
      mappings in Universal ++= contentOf(target.value / "extra"),
      update := (update dependsOn downloadBinaryDependenciesTask).value
    )
    .settings(
      Keys.fork in IntegrationTest := false,
      unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
      addTestReportOption(IntegrationTest, "int-test-reports"),
      testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
      parallelExecution in IntegrationTest := false)
    .settings(
      resolvers += Resolver.jcenterRepo
    )
    .settings(
      executableFilesInTar := Seq("wkhtmltopdf", "gs-920-linux_x86_64")
    )
}

private object TestPhases {

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}
