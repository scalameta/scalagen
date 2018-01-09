package scala.meta.gen

import java.io.File
import java.nio.file.Path

import org.scalameta.scalagen.Runner
import sbt._
import sbt.Keys._

import scala.meta._

object ScalagenTags {
  val SourceGeneration = Tags.Tag("source-generation")
}

object ScalagenPlugin extends AutoPlugin {
  override def trigger = allRequirements

  object autoImport {

    lazy val scalagenListGenerators =
      taskKey[Unit]("Lists all generators enabled")

    lazy val scalagen =
      taskKey[Seq[File]]("Applies scalagen Generators to sources")

    lazy val scalagenRecursive =
      settingKey[Boolean]("Should scalagen recursively generate definitions")

    lazy val scalagenGenerators = settingKey[Set[Generator]]("Set of Generators to use")

    lazy val generateTask = Def.task {
      GeneratorRunner(
        (unmanagedSources in Compile).value,
        scalagenGenerators.value,
        scalagenRecursive.value,
        streams.value,
        sourceDirectory.value.toPath,
        sourceManaged.value.toPath)
    }
  }

  import autoImport._

  lazy val baseGenerateSettings: Seq[Def.Setting[_]] = Seq(
    scalagenGenerators := Set.empty,
    scalagenRecursive := false,
    scalagen := generateTask.value,
    (sources in Compile) := ((sources in Compile).value.toSet[File] --
      (unmanagedSources in Compile).value.toSet[File]).toSeq,
    sourceGenerators in Compile += scalagen
  )

  override lazy val projectSettings: Seq[Def.Setting[_]] =
    baseGenerateSettings
}

object GeneratorRunner {
  def apply(
      input: Seq[File],
      generators: Set[Generator],
      recurse: Boolean,
      strm: TaskStreams,
      sourcePath: Path,
      targetPath: Path): Seq[File] = {
    input.par
      .map { f =>
        val runner = Runner(generators, recurse)

        val text = IO.read(f)
        val parsed = text.parse[Source]
        val relative = sourcePath.relativize(f.toPath)
        val outputFile = targetPath.resolve(relative).toFile
        val result: String = parsed.toOption
          .map(runner.transform(_).syntax)
          .getOrElse {
            strm.log.warn(s"Skipped ${f.name} as it could not be parsed")
            text
          }

        IO.write(outputFile, result)
        outputFile
      }
      .to[Seq]
  }
}
