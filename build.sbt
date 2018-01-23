import sbt.Keys.version

onLoadMessage := s"Welcome to scalagen ${version.value}"
name := "scalagen"

import sbt._
import sbt.Keys._

lazy val sharedSettings: Def.SettingsDefinition = Def.settings(
  updateOptions := updateOptions.value.withCachedResolution(true),
  organization := "org.scalameta",
  version := "0.1-SNAPSHOT",
  scalaVersion := "2.12.4",
  libraryDependencies ++=
    "ch.qos.logback" % "logback-classic" % "1.2.3" ::
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2" ::
      "org.scalameta" %% "scalameta" % "2.1.3" ::
      "org.scalameta" %% "contrib" % "2.1.3" ::
      "org.typelevel" %% "cats-core" % "1.0.1" ::
      "org.typelevel" %% "cats-free" % "1.0.1" ::
      "com.github.julien-truffaut" %% "monocle-core" % "1.5.0-cats" ::
      "org.scalactic" %% "scalactic" % "3.0.4" ::
      "org.scalactic" %% "scalactic" % "3.0.4" ::
      "org.scalatest" %% "scalatest" % "3.0.4" % "test" :: Nil,
  scalacOptions ++=
    "-Ypartial-unification" ::
      "-Xfatal-warnings" ::
      Nil
)

lazy val scalagen =
  project
    .in(file("scalagen"))
    .settings(
      sharedSettings,
      name := "scalagen",
      addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")
    )

// JVM sbt plugin
lazy val sbtScalagen =
  project
    .in(file("scalagen-sbt"))
    .settings(sharedSettings, sbtPlugin := true, scriptedLaunchOpts := {
      scriptedLaunchOpts.value ++
        Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
    }, scriptedBufferLog := false, moduleName := "sbt-scalagen")
    .dependsOn(scalagen)
