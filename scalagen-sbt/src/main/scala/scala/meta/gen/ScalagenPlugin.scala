package scala.meta.gen

import java.io.File
import java.nio.file.Paths

import org.scalameta.scalagen.Runner
import sbt._
import sbt.Keys._

import scala.meta._
import scala.util.matching.Regex

object ScalagenTags {
  val SourceGeneration = Tags.Tag("source-generation")
}

class ScalagenPlugin extends AutoPlugin {
  override def trigger = allRequirements


  object autoImport {

    val listGenerators =
      taskKey[Unit]("Lists all generators enabled")

    val generate =
      taskKey[Seq[File]]("Applies scalagen Generators to sources")

    val inputs = settingKey[Seq[File]]("Files to assess for generation")
    val generateRecursive = settingKey[Boolean]("Should scalagen recursively generate definitions")
    val generators = settingKey[Set[Generator]]("Set of Generators to use")

    lazy val baseGenerateSettings: Seq[Def.Setting[_]] = Seq(
      inputs := unmanagedSources.value,
      generators := Set.empty,
      generateRecursive := false,
      generate := {
        GeneratorRunner(inputs.value, generators.value, generateRecursive.value)
      }
    )
  }
  Regex.Match

  import autoImport._

  override lazy val projectSettings: Seq[Def.Setting[_]] =
    inConfig(Compile)(baseGenerateSettings) ++
      inConfig(Test)(baseGenerateSettings)

  object GeneratorRunner {
    def apply(input: Seq[File], generators: Set[Generator], recurse: Boolean): Seq[File] = {
      input.par.map { f =>
        val runner = Runner(generators, recurse)
        val sourcePath = sourceDirectory.value.toPath
        val targetPath = sourceManaged.value.toPath

        val text = IO.read(f)
        val parsed = text.parse[Source]
        val relative = sourcePath.relativize(f.toPath)
        val outputFile = targetPath.resolve(relative).toFile
        val result: String = parsed.toOption
          .map(runner.transform(_).syntax)
          .getOrElse {
            streams.value.log.warn(s"Skipped ${f.name} as it could not be parsed")
            text
          }

        IO.write(outputFile, result)
        outputFile
      }.to[Seq]
    }
  }
}
