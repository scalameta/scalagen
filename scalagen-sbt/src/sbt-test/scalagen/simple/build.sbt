import java.nio.file.Paths

import sbt._
import scala.meta._
import scala.meta.contrib._

import Generators._

version := "0.1"

scalaVersion := "2.12.4"

scalagenGenerators := Set(
  Main
)

TaskKey[Unit]("checkUntouchedHello") := {
  val relative = Paths.get("main/scala/Hello.scala")
  val input = IO.read(sourceDirectory.value.toPath.resolve(relative).toFile)
  val output = IO.read(sourceManaged.value.toPath.resolve(relative).toFile)

  val in = input.parse[Source].get
  val out = output.parse[Source].get

  if (!(in isEqual out)) {
    sys.error(s""" File was not Structurally Equal
                 |==============
                 |     In
                 |==============
                 |
                 |${in.syntax}
                 |
                 |==============
                 |     Out
                 |==============
                 |
                 |${out.syntax}
        """.stripMargin)
  }
}

val filesToCheck: Set[String] = Set("Main")

TaskKey[Unit]("checkGenerators") := {

  filesToCheck.foreach { f =>
    val relative = Paths.get(s"main/scala/$f.scala")
    val input = IO.read(sourceDirectory.value.toPath.resolve(relative).toFile)
    val output = IO.read(sourceManaged.value.toPath.resolve(relative).toFile)
    val expected = IO.read((resourceDirectory in Compile).value / s"${f}Generated.scala")

    val in = input.parse[Source].get
    val out = output.parse[Source].get
    val expec = expected.parse[Source].get

    if (!(expec isEqual out)) {
      sys.error(s""" File was not Structurally Equal
                   |==============
                   |     In
                   |==============
                   |
                   |${in.syntax}
                   |
                   |==============
                   |     Out
                   |==============
                   |
                   |${out.syntax}
                   |
                   |==============
                   |   Expected
                   |==============
                   |
                   |${expec.syntax}
                   |
        """.stripMargin)
    }
  }
}
