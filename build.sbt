import sbt.Keys.version

onLoadMessage := s"Welcome to scalagen ${version.value}"
name := "scalagen"

import sbt._
import sbt.Keys._

lazy val sharedSettings = Def.settings(
  updateOptions := updateOptions.value.withCachedResolution(true),
  organization := "org.scalameta",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.4",
  libraryDependencies ++=
    "ch.qos.logback" % "logback-classic" % "1.2.3" ::
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2" ::
      "org.scalameta" %% "scalameta" % "2.1.3" ::
      "org.scalameta" %% "contrib" % "2.1.3" ::
      "org.scalactic" %% "scalactic" % "3.0.4" ::
      "org.scalactic" %% "scalactic" % "3.0.4" ::
      "org.scalatest" %% "scalatest" % "3.0.4" % "test" :: Nil,
  scalacOptions ++= "-Xfatal-warnings" :: Nil
)

lazy val scalagen =
  project
    .in(file("scalagen"))
    .settings(sharedSettings)

// JVM sbt plugin
lazy val sbtScalagen =
  project
    .in(file("scalagen-sbt"))
    .settings(
      sharedSettings,
      sbtPlugin := true,
      moduleName := "sbt-scalagen")
    .dependsOn(scalagen)

