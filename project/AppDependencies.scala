import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  import play.core.PlayVersion

  val bootstrapVersion = "7.0.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "org.reactivemongo"  %% "play2-reactivemongo"       % "1.0.7-play28",
    "org.jsoup"           %  "jsoup"                    % "1.15.3",
    "uk.gov.hmrc"        %% "bootstrap-backend-play-28" % bootstrapVersion,
    "uk.gov.hmrc"        %% "domain"                    % "8.1.0-play-28",
    "io.github.cloudify" %% "spdf"                      % "1.4.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28" % bootstrapVersion,
    "org.pegdown"             % "pegdown"                % "1.6.0",
    "com.typesafe.play"      %% "play-test"              % PlayVersion.current,
    "org.mockito"             % "mockito-core"           % "4.7.0",
    "org.apache.pdfbox"       % "pdfbox"                 % "2.0.26"
  ).map(_ % "test")

  val it: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28" % bootstrapVersion,
    "org.pegdown"             % "pegdown"            % "1.6.0",
    "com.typesafe.play"      %% "play-test"          % PlayVersion.current
  ).map(_ % "it")

  val all: Seq[ModuleID] = compile ++ test ++ it
}
