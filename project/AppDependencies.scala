import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  import play.core.PlayVersion

  val bootstrapVersion = "8.5.0"
  val pegdownVersion = "1.6.0"
  private val playVersion = "play-30"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc.mongo"       %% s"hmrc-mongo-$playVersion"        % "1.8.0",
    "org.jsoup"                % "jsoup"                           % "1.15.3",
    "uk.gov.hmrc"             %% s"bootstrap-backend-$playVersion" % bootstrapVersion
      )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion" % bootstrapVersion,
    "org.pegdown"             % "pegdown"                      % pegdownVersion,
    "org.playframework"      %% "play-test"                    % PlayVersion.current,
    "org.mockito"             % "mockito-core"                 % "4.11.0",
    "org.apache.pdfbox"       % "pdfbox"                       % "2.0.27"
  ).map(_ % Test)

  val it: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion" % bootstrapVersion,
    "org.pegdown"             % "pegdown"                      % pegdownVersion,
    "org.playframework"      %% "play-test"                    % PlayVersion.current
  ).map(_ % Test)

  val all: Seq[ModuleID] = compile ++ test ++ it
}
