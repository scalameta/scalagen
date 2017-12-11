package org.scalameta.scalagen

import org.scalameta.scalagen.generators._

import scala.meta._
import scala.meta.contrib._

case class Runner[A <: Tree](in: A, generators: Set[Generator]) {

  val generator_cache: Map[String, Generator] =
    generators.map(g => g.name -> g).toMap

  // TODO: Make the asInstanceOf not necessary by using a custom transform
  // This also traverses pre-order, which is wrong
  // We can also optimize this, by not traversing entire tree's
  def transform: A =
    in.transform {
        case c: Defn.Class =>
          generate(c, findGenerator(c)(implicitly[Extract[Defn.Class, Mod]]))
        case o: Defn.Object =>
          generate(o, findGenerator(o)(implicitly[Extract[Defn.Object, Mod]]))
        case t: Defn.Trait =>
          generate(t, findGenerator(t)(implicitly[Extract[Defn.Trait, Mod]]))
        case other => other
      }
      .asInstanceOf[A]

  private def generate(t: Tree, generator: Generator): Tree =
    generator match {
      case gen: ExtensionGenerator if gen.generator.isDefinedAt(t) => gen.generator(t)
      case _: ExtensionGenerator => ???
      case _: Generator => ???
    }

  private def findGenerator[B <: Tree](a: B)(implicit ev: Extract[B, Mod]): Generator = {
    val generators: List[Generator] = ev.extract(a).collect {
      case Mod.Annot(Init(Type.Name(n), _, _)) if generator_cache.contains(n) =>
        generator_cache(n)
    }

    if (generators.isEmpty) {
      return IdentityGenerator
    }

    if (generators.size > 1) {
      println("Error: Multiple expansion not supported yet")
    }

    generators.head
  }
}
