import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  import play.core.PlayVersion

  val bootstrapVersion = "7.12.0"
  val pegdownVersion = "1.6.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc.mongo"  %% "hmrc-mongo-play-28"        % "0.74.0",
    "org.jsoup"           % "jsoup"                     % "1.15.3",
    "uk.gov.hmrc"        %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc"        %% "domain"                    % "8.1.0-play-28"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28" % bootstrapVersion,
    "org.pegdown"             % "pegdown"                % pegdownVersion,
    "com.typesafe.play"      %% "play-test"              % PlayVersion.current,
    "org.mockito"             % "mockito-core"           % "4.11.0",
    "org.apache.pdfbox"       % "pdfbox"                 % "2.0.27"
  ).map(_ % "test")

  val it: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28" % bootstrapVersion,
    "org.pegdown"             % "pegdown"                % pegdownVersion,
    "com.typesafe.play"      %% "play-test"              % PlayVersion.current
  ).map(_ % "it")

  val all: Seq[ModuleID] = compile ++ test ++ it
}
