package org.scalameta.scalagen

import org.scalameta.scalagen.generators.{ExtensionGenerator, Generator}

import scala.meta._
import scala.meta.contrib._

case class Runner(in: Tree, generators: Set[Generator]) {

  val annotations: Set[Mod.Annot] =
    generators
      .map(a => Mod.Annot(Init(a.name, Term.Name(""), Nil)))

  def transform: Tree = in.transform {
    case t @ NeedsGeneration(mod, generator) => generate(t, generator)
    case other => other
  }

  private def generate(t: Tree, generator: Generator): Tree =
    generator match {
      case gen: ExtensionGenerator if gen.generator.isDefinedAt(t) => gen.generator(t)
      case _: Generator => ???
    }

  object NeedsGeneration {
    def unapply(t: Tree): Option[(Mod, Generator)] =
      t match {
        case c: Defn.Class =>
          annotations
            .find(c.hasMod(_))
            .flatMap(genRunnerTuple)
        case t: Defn.Trait =>
          annotations
            .find(t.hasMod(_))
            .flatMap(genRunnerTuple)
        case o: Defn.Object =>
          annotations
            .find(o.hasMod(_))
            .flatMap(genRunnerTuple)
        case t: Defn.Type =>
          annotations
            .find(t.hasMod(_))
            .flatMap(genRunnerTuple)
        case d: Defn.Def =>
          annotations
            .find(d.hasMod(_))
            .flatMap(genRunnerTuple)
        case v: Defn.Val =>
          annotations
            .find(v.hasMod(_))
            .flatMap(genRunnerTuple)
        case v: Defn.Var =>
          annotations
            .find(v.hasMod(_))
            .flatMap(genRunnerTuple)
      }
  }
  private def genRunnerTuple(a: Mod.Annot) =
    generators
      .find(g => a.init.name isEqual g.name)
      .map(g => (a, g))
}
