package org.scalameta.scalagen

import org.scalameta.scalagen.generators._

import scala.meta._
import scala.meta.contrib._

import scala.collection.breakOut

/**
  * Will transform only extension generators.
  * But is the fastest and easiest to implement
  */
case class ManipulationRunner(generators: Set[Generator], recurse: Boolean = false) {
  val generator_cache: Map[String, Generator] =
    generators.map(g => g.name -> g).toMap

  // TODO: Make the asInstanceOf not necessary by using a custom transform
  // This also traverses pre-order, which is wrong
  // We can also optimize this, by not traversing entire tree's
  def transform[A <: Tree](in: A): A =
    in.transform {
        case c: Defn.Class =>
          generate(c, findGenerators(c))
        case o: Defn.Object =>
          generate(o, findGenerators(o))
        case t: Defn.Trait =>
          generate(t, findGenerators(t))
        case d: Defn.Def =>
          generate(d, findGenerators(d))
        case v: Defn.Val =>
          generate(v, findGenerators(v))
        case v: Defn.Var =>
          generate(v, findGenerators(v))
        case other => other
      }
      .asInstanceOf[A]

  // TODO: Simplify
  private def generate(t: Tree, generator: List[ManipulationGenerator]): Tree = {
    val generators: Set[String] = generator.map(_.name)(breakOut)
    val result: Tree = t match {
      case c: Defn.Class =>
        generator.foldLeft(c)((current, g) => removeExpandedAnnots(g.manipulate(current), Set(g.name)))
      case t: Defn.Trait =>
        generator.foldLeft(t)((current, g) => removeExpandedAnnots(g.manipulate(current), Set(g.name)))
      case o: Defn.Object =>
        generator.foldLeft(o)((current, g) => removeExpandedAnnots(g.manipulate(current), Set(g.name)))
      case d: Defn.Def =>
        generator.foldLeft(d)((current, g) => removeExpandedAnnots(g.manipulate(current), Set(g.name)))
      case v: Defn.Val =>
        generator.foldLeft(v)((current, g) => removeExpandedAnnots(g.manipulate(current), Set(g.name)))
      case v: Defn.Var =>
        generator.foldLeft(v)((current, g) => removeExpandedAnnots(g.manipulate(current), Set(g.name)))
    }

    // If we are expanding recursive definions
    if (recurse) {
      // Aka did not noop
      if (!result.isEqual(t)) {
        return transform(result)
      }
    }

    result
  }

  private def removeExpandedAnnots[A <: Tree](t: A, expanded: Set[String])(
      implicit ev: Replace[A, Mod],
      ev2: Extract[A, Mod]): A = {
    val newMods: List[Mod] = t.extract[Mod].collect {
      case m @ Mod.Annot(Init(Type.Name(name), _, _)) if !expanded.contains(name) => m
    }

    t.withMods(newMods)
  }

  private def findGenerators[B <: Tree](a: B)(
      implicit ev: Extract[B, Mod]): List[ManipulationGenerator] = {
    ev.extract(a).collect {
      case Mod.Annot(Init(Type.Name(n), _, _)) if generator_cache.contains(n) =>
        generator_cache(n) match {
          case e: ManipulationGenerator => e
          case _ =>
            throw new IllegalStateException(
              "The manipulation runner can only handle Manipulation Generators")
        }
    }
  }
}
