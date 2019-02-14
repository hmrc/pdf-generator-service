import play.routes.compiler.StaticRoutesGenerator
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import com.typesafe.sbt.SbtNativePackager._


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
      evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
      routesGenerator := StaticRoutesGenerator
    )
    .configs(IntegrationTest)
    .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
    .settings(
      downloadBinaryDependenciesTask := {

        import scala.sys.process._
        val tempDir = IO.createTemporaryDirectory
        val extraDir = target.value / "extra"
        val binDir: File = extraDir / "bin"
        val ghostscript = new File(tempDir, "ghostscript.tgz")
        extraDir.mkdir()
        binDir.mkdir()
        IO.download(new URL("https://dl.bintray.com/hmrc/releases/uk/gov/hmrc/ghostscript/ghostscript-9.20-linux-x86_64.tgz"), ghostscript)
        s"tar zxf ${ghostscript.absolutePath} -C ${binDir.getAbsoluteFile} --strip-components 1".!
        s"chmod +x ${binDir.getAbsoluteFile}/gs-920-linux_x86_64".!
        ghostscript.delete()

        val wkhtmltox = new File(tempDir, "tgz")
        IO.download(new URL("https://dl.bintray.com/hmrc/releases/uk/gov/hmrc/wkhtmltox/wkhtmltox-0.12.4_linux-generic-amd64.tar.xz"), wkhtmltox)
        s"tar xJf ${wkhtmltox.absolutePath} -C ${extraDir.getAbsoluteFile} --strip-components 1".!
        s"chmod +x ${binDir.getAbsoluteFile}/wkhtmltopdf".!
        wkhtmltox.delete()
      },
      mappings in Universal ++= contentOf(target.value / "extra"),
      distTgzTask := (distTgzTask dependsOn downloadBinaryDependenciesTask).value
    )
    .settings(
      Keys.fork in IntegrationTest := false,
      unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
      addTestReportOption(IntegrationTest, "int-test-reports"),
      testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
      parallelExecution in IntegrationTest := false)
    .settings(
      resolvers += Resolver.bintrayRepo("hmrc", "releases"),
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
