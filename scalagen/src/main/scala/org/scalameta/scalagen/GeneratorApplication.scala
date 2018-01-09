package org.scalameta.scalagen

import scala.meta._
import scala.meta.contrib._
import scala.meta.gen._

/**
  * All transformations, manipulations etc. should completely noop, by default.
  *
  * They should return the exact identical object thats inputed.
  *
  * Structural equality is slow, thus we want to remain as efficient as possible here.
  */
object GeneratorApplication {

  def apply(c: Defn.Class, g: Generator): Defn.Class = {

    generateCompanionExtensions(c, g)

    val extended: Defn.Class = c.prepend(g.extend(c))
    val transformed: Defn.Class = g.manipulate(c)
    val transmuted: List[Stat] = g.transmute(c)

    val wasExtended = extended ne c
    val wasTransformed = transformed ne c
    val wasTransmuted = !(transmuted.lengthCompare(1) == 0 && (transmuted.head eq c))

    if (wasTransformedMultipleTimes(wasExtended, wasTransformed, wasTransmuted)) {
      abortDueToMultipleTransform(c, g)
    }

    if (wasExtended) {
      extended
    } else if (wasTransformed) {
      transformed
    } else if (wasTransmuted) {
      populateTransmutations(c, transmuted, g)
      c
    } else {
      c
    }
  }

  private def abortDueToMultipleTransform(c: Defn.Class, g: Generator) = {
    abort(
      s"""Generator: ${g.name} tried to transform the tree ${c.name.syntax} multiple times
         |
          |This error can appear when a generator implements more then one of the following
         |
          | extend()
         | transmute()
         | manipulate()
         |
        """.stripMargin)
  }

  private def wasTransformedMultipleTimes(booleans: Boolean*) =
    booleans.count(_ == true) > 1

  private def populateTransmutations(t: Tree, stats: List[Stat], g: Generator): Unit = {
    t.owner match {
      case Some(own) => ???
      case _ =>
    }
  }

  private def generateCompanionExtensions(c: Defn.Class, g: Generator): Unit = {
    g.extendCompanion(c) match {
      case Nil =>
      case other => ???
    }
  }

}
