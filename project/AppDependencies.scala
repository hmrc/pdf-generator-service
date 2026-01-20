import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  import play.core.PlayVersion

  val bootstrapVersion = "10.5.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc.mongo"  %% "hmrc-mongo-play-30"        % "2.10.0",
    "org.jsoup"           % "jsoup"                     % "1.15.3",
    "uk.gov.hmrc"        %% "bootstrap-backend-play-30" % bootstrapVersion,
    "uk.gov.hmrc"        %% "domain-play-30"                    % "13.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.playframework"      %% "play-test"              % "3.0.9",
    "org.mockito"             % "mockito-core"           % "5.21.0",
    "org.apache.pdfbox"       % "pdfbox"                 % "3.0.6"
  ).map(_ % "test")

  val it: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.playframework"      %% "play-test"              % "3.0.9"
  ).map(_ % "it")

  val all: Seq[ModuleID] = compile ++ test ++ it
}
