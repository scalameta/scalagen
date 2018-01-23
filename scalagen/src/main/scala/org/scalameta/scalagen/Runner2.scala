package org.scalameta.scalagen

import org.scalameta.scalagen.GeneratorTree.GeneratorTree

import scala.meta.{XtensionShow => _, _}
import scala.meta.gen._

object Runner2 {

  def apply(t: Tree, gens: Set[Generator]): Option[Tree] =
    expandGenerators(GeneratorTree(t), gens)

  private def expandGenerators(t: GeneratorTree, gs: Set[Generator]): Option[Tree] = ???
}