package org.scalameta.scalagen

import org.scalameta.scalagen.generators._

import scala.meta._
import scala.meta.contrib._

import scala.collection.breakOut

/**
  * Will transform only extension generators.
  * But is the fastest and easiest to implement
  */
case class ExtensionRunner(generators: Set[Generator], recurse: Boolean = false) {
  val generator_cache: Map[String, Generator] =
    generators.map(g => g.name -> g).toMap

  // TODO: Make the asInstanceOf not necessary by using a custom transform
  // This also traverses pre-order, which is wrong
  // We can also optimize this, by not traversing entire tree's
  def transform[A <: Tree](in: A): A =
    in.transform {
        case c: Defn.Class =>
          generate(c, findGenerators(c)(implicitly[Extract[Defn.Class, Mod]]))
        case o: Defn.Object =>
          generate(o, findGenerators(o)(implicitly[Extract[Defn.Object, Mod]]))
        case t: Defn.Trait =>
          generate(t, findGenerators(t)(implicitly[Extract[Defn.Trait, Mod]]))
        case other => other
      }
      .asInstanceOf[A]

  // TODO: Simplify
  private def generate(t: Tree, generator: List[ExtensionGenerator]): Tree = {
    val result: Tree = t match {
      case c: Defn.Class =>
        removeExpandedAnnots(
          c.withStats(c.extract[Stat] ::: generator.flatMap(_.extend(c))),
          generator.map(_.name)(breakOut)
        )
      case t: Defn.Trait =>
        removeExpandedAnnots(
          t.withStats(t.extract[Stat] ::: generator.flatMap(_.extend(t))),
          generator.map(_.name)(breakOut)
        )
      case o: Defn.Object =>
        removeExpandedAnnots(
          o.withStats(o.extract[Stat] ::: generator.flatMap(_.extend(o))),
          generator.map(_.name)(breakOut)
        )
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
      implicit ev: Extract[B, Mod]): List[ExtensionGenerator] = {
    ev.extract(a).collect {
      case Mod.Annot(Init(Type.Name(n), _, _)) if generator_cache.contains(n) =>
        generator_cache(n) match {
          case e: ExtensionGenerator => e
          case _ =>
            throw new IllegalStateException(
              "The extension runner can only handle Extension Generators")
        }
    }
  }
}
