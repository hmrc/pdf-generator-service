import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  import play.core.PlayVersion

  val compile: Seq[ModuleID] = Seq(
    ws,
    "org.reactivemongo"  %% "play2-reactivemongo"       % "1.0.7-play28",
    "org.jsoup"           %  "jsoup"                    % "1.14.2",
    "uk.gov.hmrc"        %% "bootstrap-backend-play-28" % "5.12.0",
    "uk.gov.hmrc"        %% "domain"                    % "6.2.0-play-28",
    "io.github.cloudify" %% "spdf"                      % "1.4.0"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"          % "3.0.8",
    "com.vladsch.flexmark"    % "flexmark-all"       % "0.35.10",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0",
    "org.pegdown"             % "pegdown"            % "1.6.0",
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current,
    "org.mockito"             % "mockito-core"       % "3.6.0",
    "org.apache.pdfbox"       % "pdfbox"             % "2.0.13"
  ).map(_ % "test")

  val it: Seq[ModuleID] = Seq(
    "org.scalatest"          %% "scalatest"          % "3.0.8",
    "com.vladsch.flexmark"    % "flexmark-all"       % "0.35.10",
    "org.pegdown"             % "pegdown"            % "1.6.0",
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0"
  ).map(_ % "it")

  val all: Seq[ModuleID] = compile ++ test ++ it
}