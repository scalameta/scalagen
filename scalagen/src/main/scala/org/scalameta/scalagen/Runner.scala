package org.scalameta.scalagen

import scala.meta._
import scala.meta.contrib._
import scala.meta.gen._
import scala.collection.mutable
import scala.meta.contrib.equality.Structurally
import scala.meta.gen.{
  CompanionGenerator,
  ExtensionGenerator,
  Generator,
  ManipulationGenerator,
  TransmutationGenerator
}

case class TransmutationResult(in: Stat, out: List[Defn])

/**
  * Will transform only extension generators.
  * But is the fastest and easiest to implement
  */
case class Runner(generators: Set[Generator], recurse: Boolean = false) {
  val generator_cache: Map[String, Generator] =
    generators.map(g => g.name -> g).toMap

  // These are necessary due to restrictions with how we can traverse the tree.
  // It's possible that with more transformation strategies, these would be unnecessary.
  val transmutationCache: mutable.Map[Structurally[Tree], TransmutationResult] = mutable.Map()
  val companionExtensionCache: mutable.Map[String, List[Stat]] = mutable.Map()

  // TODO: Make the asInstanceOf not necessary by using a custom transform
  // We can also optimize this, by not traversing entire tree's
  def transform[A <: Tree](in: A): A = {
    in.leafFirstTransform {
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
        case other => finalizeCompanion(finalizeTransmutations(other))
      }
      .asInstanceOf[A]
  }

  private def generate(t: Tree, generator: List[Generator]): Tree = {
    val postTransmutate = finalizeTransmutations(t)
    val postCompanion = finalizeCompanion(postTransmutate)
    val result = generator.foldLeft(postCompanion)((c, g) => applyGenerator(c, g))

    // If we are expanding recursive definitions
    if (recurse && !result.isEqual(t)) {
      transform(result)
    } else {
      result
    }
  }

  /**
    * Add children that have been waiting to be added via transmutation.
    *
    * Note: There is no annotation to remove for this.
    *
    * TODO: This list is not complete
    */
  private def finalizeTransmutations(t: Tree): Tree = {
    t match {
      case templ: Template if transmutationCache.contains(templ.withStats(Nil)) =>
        applyTransmutationResult(templ, transmutationCache.remove(templ.withStats(Nil)).get)
      case pkg: Pkg if transmutationCache.contains(pkg.withStats(Nil)) =>
        applyTransmutationResult(pkg, transmutationCache.remove(pkg.withStats(Nil)).get)
      case b: Term.Block if transmutationCache.contains(b.withStats(Nil)) =>
        applyTransmutationResult(b, transmutationCache.remove(b.withStats(Nil)).get)
      case s: Source if transmutationCache.contains(s.withStats(Nil)) =>
        applyTransmutationResult(s, transmutationCache.remove(s.withStats(Nil)).get)
      case other => other
    }
  }

  /**
    * Remove the original defn. and replace is with the new list of defns.
    *
    * Note: Order is not preserved, new Defn's are put at the end of the class.
    */
  private def applyTransmutationResult[A <: Tree: StatReplacer: StatExtractor](
      a: A,
      r: TransmutationResult): A =
    a.withStats(a.extract[Stat].filterNot(t => t isEqual r.in) ::: r.out)

  /**
    * Add stats that have been waiting to be added.
    *
    * Note: There is no annotation to remove for this.
    */
  // TODO: This is kind of unsafe until we have semantic discovery
  // But in reality, it will be the first companion encountered with this name,
  // which is guaranteed to be the one that is targeted.
  private def finalizeCompanion(tree: Tree): Tree = {
    tree match {
      case o: Defn.Object =>
        val stats = companionExtensionCache.remove(o.name.value).getOrElse(Nil)
        o.withStats(o.extract[Stat] ::: stats)
      case other => other
    }
  }

  private def applyGenerator[A <: Tree](in: A, g: Generator): A = {
    g match {
      case m: ManipulationGenerator => applyManipulator(in, m)
      case e: ExtensionGenerator => applyExtender(in, e)
      case c: CompanionGenerator => applyCompanionExtender(in, c)
      case t: TransmutationGenerator => applyTransmutator(in, t)
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

  private def genCompanion(name: Term.Name, stats: List[Stat]): Defn.Object =
    q"object $name { ..$stats }"

  private def applyCompanionExtender[A <: Tree](in: A, g: CompanionGenerator): A = {
    assert(
      in.parent.isDefined,
      s"Companion generator '${g.name}' relies on the annotee having a parent")

    val res = in match {
      case c: Defn.Class =>
        val stats = g.extendCompanion(c)
        val clazzWithoutAnnot: Defn.Class = removeAnnot(c, g.name)
        if (c.companionObject.isEmpty) {
          val defns: List[Defn] = clazzWithoutAnnot :: genCompanion(c.name.asTerm, stats) :: Nil
          transmutationCache.put(
            Structurally(withoutStats(c.parent.get)),
            TransmutationResult(clazzWithoutAnnot, defns))
        } else {
          //TODO: Check that the companion has not already been transformed
          companionExtensionCache.put(c.name.value, stats)
        }
        clazzWithoutAnnot
      case t: Defn.Type =>
        val stats = g.extendCompanion(t)
        val typeWithoutAnnot: Defn.Type = removeAnnot(t, g.name)
        if (t.companionObject.isEmpty) {
          val defns: List[Defn] = typeWithoutAnnot :: genCompanion(t.name.asTerm, stats) :: Nil
          transmutationCache.put(
            Structurally(withoutStats(t.parent.get)),
            TransmutationResult(typeWithoutAnnot, defns))
        } else {
          //TODO: Check that the companion has not already been transformed
          companionExtensionCache.put(t.name.value, stats)
        }
        typeWithoutAnnot
      case t: Defn.Trait =>
        val stats = g.extendCompanion(t)
        val traitWithoutAnnot: Defn.Trait = removeAnnot(t, g.name)
        if (t.companionObject.isEmpty) {
          val defns: List[Defn] = traitWithoutAnnot :: genCompanion(t.name.asTerm, stats) :: Nil
          transmutationCache.put(
            Structurally(withoutStats(t.parent.get)),
            TransmutationResult(traitWithoutAnnot, defns))
        } else {
          //TODO: Check that the companion has not already been transformed
          companionExtensionCache.put(t.name.value, stats)
        }
        traitWithoutAnnot
    }

    res.asInstanceOf[A]
  }

  // TODO: This method should not be necessary
  // We need the ability to reparent during transformation.
  private def withoutStats[A <: Tree](t: A): A = {
    val res = t match {
      case templ: Template =>
        templ.withStats(Nil)
      case pkg: Pkg =>
        pkg.withStats(Nil)
      case b: Term.Block =>
        b.withStats(Nil)
      case s: Source =>
        s.withStats(Nil)
      case other => other
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

  /**
    * Transmutation works by deferring any replacement until the parent is about to be transformed.
    * Transmutations are applied before the parent is transformed, but after
    * ALL children have had their generators applied.
    *
    * We store a map of parent => (child, defns)
    *
    * The parent is the "holder" of stats. eg. a Term.Block/Template/Pkg etc.
    *
    * The child is the input tree "without" the transmutation annotation.
    * This is so we can search for, and replace it with the new Defn's, when the parent is tranformed.
    *
    * The defns are just a list of definitions being replaced.
    */
  private def applyTransmutator[A <: Tree](in: A, g: TransmutationGenerator): A = {
    assert(
      in.parent.isDefined,
      s"Transmutation generator '${g.name}' relies on the annotee having a parent")
    val res = in match {
      case c: Defn.Class =>
        val clazzWithoutAnnot = removeAnnot(c, g.name)
        transmutationCache.put(
          Structurally(withoutStats(c.parent.get)),
          TransmutationResult(clazzWithoutAnnot, g.transmute(c)))
        clazzWithoutAnnot
      case o: Defn.Object =>
        val objectWithoutAnnot = removeAnnot(o, g.name)
        transmutationCache.put(
          Structurally(withoutStats(o.parent.get)),
          TransmutationResult(objectWithoutAnnot, g.transmute(o)))
        objectWithoutAnnot
      case t: Defn.Trait =>
        val traitWithoutAnnot = removeAnnot(t, g.name)
        transmutationCache.put(
          Structurally(withoutStats(t.parent.get)),
          TransmutationResult(traitWithoutAnnot, g.transmute(t)))
        traitWithoutAnnot
      case t: Defn.Type =>
        val tpeWithoutAnnot = removeAnnot(t, g.name)
        transmutationCache.put(
          Structurally(withoutStats(t.parent.get)),
          TransmutationResult(tpeWithoutAnnot, g.transmute(t)))
        tpeWithoutAnnot
      case d: Defn.Def =>
        val defWithoutAnnot = removeAnnot(d, g.name)
        transmutationCache.put(
          Structurally(withoutStats(d.parent.get)),
          TransmutationResult(defWithoutAnnot, g.transmute(d)))
        defWithoutAnnot
      case v: Defn.Val =>
        val valWithoutAnnot = removeAnnot(v, g.name)
        transmutationCache.put(
          Structurally(withoutStats(v.parent.get)),
          TransmutationResult(valWithoutAnnot, g.transmute(v)))
        valWithoutAnnot
      case v: Defn.Var =>
        val varWithoutAnnot = removeAnnot(v, g.name)
        transmutationCache.put(
          Structurally(withoutStats(v.parent.get)),
          TransmutationResult(varWithoutAnnot, g.transmute(v)))
        varWithoutAnnot
    }

    res.asInstanceOf[A]
  }

  // Todo, consider an implicit for this
  private def removeAnnot[A <: Tree: ModReplacer: ModExtractor](t: A, expanded: String): A = {
    val toRemove: Option[Mod] = t.extract[Mod].collectFirst {
      case m @ Mod.Annot(Init(Type.Name(name), _, _)) if expanded == name => m
    }

    // Identity check is on purpose here. To prevent removing duplicate annots
    val newMods: List[Mod] = t.extract[Mod].collect {
      case a if !toRemove.contains(a) => a
    }

    t.withMods(newMods)
  }

  /**
    * Find generators in the list of annotations of this tree.
    */
  private def findGenerators[B <: Tree: ModExtractor](a: B): List[Generator] = {
    a.extract[Mod].collect {
      case Mod.Annot(Init(Type.Name(n), _, _)) if generator_cache.contains(n) =>
        generator_cache(n) match {
          case m: ManipulationGenerator => m
          case e: ExtensionGenerator => e
          case t: TransmutationGenerator => t
          case c: CompanionGenerator => c
          case g =>
            throw new IllegalStateException(
              s"The runner cannot handle this type of generator: ${g.getClass.getSimpleName}")
        }
    }
  }
}
