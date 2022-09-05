resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)

resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("uk.gov.hmrc" % "sbt-auto-build" % "3.8.0")

libraryDependencies ++= Seq("net.databinder.dispatch" %% "dispatch-core" % "0.13.4")

addSbtPlugin("uk.gov.hmrc" % "sbt-git-versioning" % "2.4.0")

addSbtPlugin("uk.gov.hmrc" % "sbt-distributables" % "2.1.0")

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.16")

addSbtPlugin("uk.gov.hmrc" % "sbt-artifactory" % "2.0.0")

addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.16")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.9.3")
