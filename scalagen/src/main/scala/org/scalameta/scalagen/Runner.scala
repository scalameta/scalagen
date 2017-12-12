package org.scalameta.scalagen

import org.scalameta.scalagen.generators._

import scala.meta._
import scala.meta.contrib._

import scala.collection.breakOut

/**
  * Will transform only extension generators.
  * But is the fastest and easiest to implement
  */
case class Runner(generators: Set[Generator], recurse: Boolean = false) {
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

  private def generate(t: Tree, generator: List[Generator]): Tree = {
    val generators: Set[String] = generator.map(_.name)(breakOut)
    val result = generator.foldLeft(t)((c, g) => applyGenerator(c, g))

    // If we are expanding recursive definions
    if (recurse) {
      // Aka did not noop

      if (result.syntax != t.syntax) {
        return transform(result)
      }
    }

    result
  }

  private def applyGenerator[A <: Tree](in: A, g: Generator): A =  {
    g match {
      case m: ManipulationGenerator => applyManipulator(in, m)
      case e: ExtensionGenerator => applyExtender(in, e)
    }
  }

  private def applyExtender[A <: Tree](in: A, g: ExtensionGenerator): A = {
    val res = in match {
      case c: Defn.Class =>
        val clazz = c.withStats(c.extract[Stat] ::: g.extend(c))
        removeAnnot(clazz, g.name)
      case o: Defn.Object =>
        val obj = o.withStats(o.extract[Stat] ::: g.extend(o))
        removeAnnot(obj, g.name)
      case t: Defn.Trait =>
        val trat = t.withStats(t.extract[Stat] ::: g.extend(t))
        removeAnnot(trat, g.name)
    }

    res.asInstanceOf[A]
  }
  private def applyManipulator[A <: Tree](in: A, g: ManipulationGenerator): A = {
    val res = in match {
      case c: Defn.Class =>
        val clazz = g.manipulate(c)
        removeAnnot(clazz, g.name)
      case o: Defn.Object =>
        val obj = g.manipulate(o)
        removeAnnot(obj, g.name)
      case t: Defn.Trait =>
        val trat = g.manipulate(t)
        removeAnnot(trat, g.name)
      case d: Defn.Def =>
        val deff = g.manipulate(d)
        removeAnnot(deff, g.name)
      case v: Defn.Val =>
        val vall = g.manipulate(v)
        removeAnnot(vall, g.name)
      case v: Defn.Var =>
        val varr = g.manipulate(v)
        removeAnnot(varr, g.name)
    }

    res.asInstanceOf[A]
  }

  // Todo, consider an implicit for this
  private def removeAnnot[A <: Tree](t: A, expanded: String)(
      implicit ev: Replace[A, Mod],
      ev2: Extract[A, Mod]): A = {
    val toRemove: Option[Mod] = t.extract[Mod].collectFirst {
      case m @ Mod.Annot(Init(Type.Name(name), _, _)) if expanded == name => m
    }

    // Identity check is on purpose here. To prevent removing duplicate annots
    val newMods: List[Mod] = t.extract[Mod].collect {
      case a if !toRemove.contains(a) => a
    }

    t.withMods(newMods)
  }

  private def findGenerators[B <: Tree](a: B)(
      implicit ev: Extract[B, Mod]): List[Generator] = {
    ev.extract(a).collect {
      case Mod.Annot(Init(Type.Name(n), _, _)) if generator_cache.contains(n) =>
        generator_cache(n) match {
          case m: ManipulationGenerator => m
          case e: ExtensionGenerator => e
          case _ =>
            throw new IllegalStateException(
              "The runner can only handle Manipulation and Extension Generators")
        }
    }
  }
}
