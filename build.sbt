import sbt.{Credentials, Developer, ScmInfo}

import com.sksamuel.scapegoat.sbt.ScapegoatSbtPlugin.autoImport._
import ch.epfl.scala.sbt.release.ReleaseEarlyPlugin.autoImport._

lazy val commonSettings = Seq(
  organization := "org.combinators",
  scalaVersion := "2.12.10",
  crossScalaVersions := Seq("2.11.12", scalaVersion.value),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.typesafeRepo("releases")
  ),

  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:implicitConversions"
  ),

  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.1.0" % "test",
    "org.scalatestplus" %% "scalacheck-1-14" % "3.1.0.1" % "test",
    "org.scalacheck" %% "scalacheck" % "1.14.1" % "test" 
  ),
  headerLicense := Some(HeaderLicense.ALv2("2020", "Jan Bessai")),
  scapegoatVersion in ThisBuild := "1.4.1",
  concurrentRestrictions in Global ++= {
    if (sys.env.get("CI") == Some("github")) Seq(Tags.limitAll(1)) else Seq.empty
  }
) ++ publishSettings

lazy val root =
  Project(id = "jgitserv", base = file("."))
    .settings(commonSettings: _*)
    .settings(
      moduleName := "jgitserv",
      libraryDependencies ++= Seq(
        "org.combinators" %% "templating" % "1.1.0",
        "com.github.finagle" %% "finchx-core" % "0.31.0",
        "org.eclipse.jgit" % "org.eclipse.jgit" % "5.4.0.201906121030-r",
        "commons-io" % "commons-io" % "2.6",
        "ch.qos.logback" % "logback-classic" % "1.2.3"
      )
    )

lazy val publishSettings = Seq(
  homepage := Some(url("https://www.github.com/combinators/jgitserv")),
  licenses := Seq(
    "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")
  ),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/combinators/jgitserv"),
      "scm:git:git@github.com:combinators/jgitserv.git"
    )
  ),
  developers := List(
    Developer(
      "JanBessai",
      "Jan Bessai",
      "jan.bessai@tu-dortmund.de",
      url("http://janbessai.github.io")
    )
  ),
  version := "0.0.2",
  releaseEarlyWith := SonatypePublisher
)

lazy val noPublishSettings = Seq(
  publish := Seq.empty,
  publishLocal := Seq.empty,
  publishArtifact := false
)
