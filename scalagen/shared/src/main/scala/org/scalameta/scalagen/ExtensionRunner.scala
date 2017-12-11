package org.scalameta.scalagen

import org.scalameta.scalagen.generators._

import scala.meta._
import scala.meta.contrib._

/**
 * Will transform only extension generators.
 * But is the fastest and easiest to implement
 */
case class ExtensionRunner(generators: Set[Generator]) {

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

  private def generate(t: Tree, generator: List[ExtensionGenerator]): Tree =
    t match {
      case c: Defn.Class =>
        c.withStats(c.extract[Stat] ::: generator.flatMap(_.extend(c)))
      case t: Defn.Trait =>
        t.withStats(t.extract[Stat] ::: generator.flatMap(_.extend(t)))
      case o: Defn.Object =>
        o.withStats(o.extract[Stat] ::: generator.flatMap(_.extend(o)))
    }

  private def findGenerators[B <: Tree](a: B)(implicit ev: Extract[B, Mod]): List[ExtensionGenerator] = {
    ev.extract(a).collect {
      case Mod.Annot(Init(Type.Name(n), _, _)) if generator_cache.contains(n) =>
        generator_cache(n) match {
          case e: ExtensionGenerator => e
          case _ => throw new IllegalStateException(
            "The extension runner can only handle Extension Generators")
        }
    }
  }
}
