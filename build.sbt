/*
 * Plugins
 */
enablePlugins(GatlingPlugin)

lazy val unusedWarnings = Seq("-Ywarn-unused-import", "-Ywarn-unused")

lazy val commonSettings: Seq[Setting[_]] = Seq(
  organization in ThisBuild := "com.godatadriven.gatling",
  homepage in ThisBuild := Some(url(s"https://github.com/krisgeus/gatling-${name.value}/#readme")),
  licenses in ThisBuild := Seq(("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))),
  description in ThisBuild := "Gatling Feeder for using Druid queries as Feed",
  developers in ThisBuild := List(
    Developer("krisgeus", "Kris Geusebroek", "@krisgeus", url("https://github.com/krisgeus"))
  ),
  scmInfo in ThisBuild := Some(
    ScmInfo(
      url(s"https://github.com/krisgeus/gatling-${name.value}"),
      s"git@github.com:krisgeus/gatling-${name.value}.git"
    )
  ),
  crossScalaVersions in ThisBuild := Seq("2.12.3"),
  scalaVersion in ThisBuild := "2.12.3",
  scalacOptions ++= Seq(Opts.compile.deprecation, "-Xlint", "-feature"),
  scalacOptions ++= PartialFunction.condOpt(CrossVersion.partialVersion(scalaVersion.value)) {
    case Some((2, v)) if v >= 11 => unusedWarnings
  }.toList.flatten,
  publishArtifact in Test := false
) ++ Seq(Compile, Test).flatMap(c =>
  scalacOptions in (c, console) --= unusedWarnings
)

lazy val root = (project in file("."))
  .settings(commonSettings)
  .settings(
    name := "druid-feeder",
    organization := "com.godatadriven.gatling",
    organizationName := "GoDataDriven",
    version := "0.1-SNAPSHOT",
    /*
     * Dependencies
     */
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % DependencyVersions.typesafeConfig,

      "org.json4s" %% "json4s-jackson" % DependencyVersions.json4s,
      "org.json4s" %% "json4s-ext" % DependencyVersions.json4s,

      "ing.wbaa.druid" %% "scruid" % DependencyVersions.scruid excludeAll ExclusionRule(organization = "com.typesafe.akka"),

      "ch.qos.logback" % "logback-classic" % DependencyVersions.logback,

      //Akka is picky when having multiple versions, so specify the exact version here and exclude everywhere else.
      "com.typesafe.akka" %% "akka-http" % "10.0.9"
        excludeAll ExclusionRule(organization = "com.typesafe.akka", artifact = "akka-stream"),
      "com.typesafe.akka" %% "akka-stream" % "2.5.2",

      "io.gatling" % "gatling-test-framework" % "3.0.0-SNAPSHOT",
      "io.gatling.highcharts" % "gatling-charts-highcharts" % "3.0.0-SNAPSHOT",

      "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    ),
    resolvers ++= Seq(Resolver.sonatypeRepo("releases"), Resolver.jcenterRepo,
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
  )
