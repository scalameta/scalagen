package org.scalameta.scalagen

import cats._
import cats.free._
import cats.implicits._
import monocle._

import scala.meta.{XtensionShow => _, _}
import scala.meta.gen.TypeclassExtractors.retrieveAnnotExtractInstance
import scala.meta.gen._

case class GeneratorInputContext(src: Tree, toExpand: List[Generator])
object GeneratorTree {

  /**
    * OwnerTree is just simple tree with an arbritraty ,
    * The issue is we cannot use the name Tree as we do not want
    * conflicts with scalameta.tree
    *
    * TODO: Consider moving this out of scalagen
    */
  type GeneratorTree = Cofree[List, GeneratorInputContext]

  /**
    * Partially applied alias for OwnerTree. Allows use as a Functor/Monad etc.
    */
  type GeneratorTreeF[A] = Cofree[List, A]

  /**
    * Lazily build a Cofree from this tree.
    *
    * Cofree objects are only created as the tree is traversed.
    */
  def apply(t: Tree): GeneratorTreeF[Tree] =
    Cofree(t, Eval.later(t.children.map(apply)))

  /**
    * Produce a product prefix based tree representation.
    *
    * Includes all nodes of the tree.
    *
    * Defn.Class
    *  - Defn.Var
    *    - Defn.Def
    */
  implicit def treeShowInstance: Show[GeneratorTree] =
    Show.show[GeneratorTree](genTraversalString(regularTraversal, _))

  /**
    * Will print all nodes visited by the given traversal
    */
  def genTraversalString(
      t: Traversal[GeneratorTreeF[GeneratorInputContext], GeneratorInputContext],
      ot: GeneratorTree): String = {
    val childString =
      ot.tailForced
        .map(genTraversalString(t, _))
        .filterNot(_.isEmpty)
        .flatMap(_.lines.toList)
        .mkString("\n  ")

    val res = t.headOption(ot) match {
      case None if childString.isEmpty =>
        ""
      case None =>
        error(childString)
        childString
      case Some(ctx) if childString.isEmpty =>
        s" - ${treePrefixAndName(ctx.src)}"
      case Some(ctx) =>
        s""" - ${treePrefixAndName(ctx.src)}
           |  $childString""".stripMargin
    }

    res
  }

  /**
    * Primarily used for debug
    *
    * For example
    * "Defn.Class: Foo"
    *
    * TODO: Make an extract/replace instance for names
    */
  private def treePrefixAndName(tree: Tree) = {
    val nameStr =
      tree match {
        case o: Pkg => o.ref.syntax + "." + o.name.syntax
        case o: Defn.Object => o.name.syntax
        case c: Defn.Class => c.name.syntax
        case t: Defn.Trait => t.name.syntax
        case t: Defn.Type => t.name.syntax
        case t: Decl.Type => t.name.syntax
        case d: Defn.Def => d.name.syntax
        case d: Decl.Def => d.name.syntax
        case v: Defn.Val => genNameSyntax(v.pats)
        case v: Decl.Val => genNameSyntax(v.pats)
        case v: Defn.Var => genNameSyntax(v.pats)
        case v: Decl.Var => genNameSyntax(v.pats)
        case s: Term.Select => s.syntax
        case s: Type.Select => s.syntax
        case n: Term.Name => n.syntax
        case n: Type.Name => n.syntax
        case _ => ""
      }
    if (nameStr.isEmpty) {
      tree.productPrefix
    } else {
      tree.productPrefix + s": $nameStr"
    }
  }

  private def genNameSyntax(pats: List[Pat]): String =
    pats
      .collect({ case Pat.Var(name) => name })
      .mkString(", ")

  def regularTraversal[A]: Traversal[GeneratorTreeF[A], A] =
    Traversal.fromTraverse[GeneratorTreeF, A](Traverse[GeneratorTreeF])

  val ownerPrism: Prism[GeneratorTreeF[Tree], GeneratorTreeF[Tree]] =
    Prism[GeneratorTreeF[Tree], GeneratorTreeF[Tree]](t => {
      if (t.head.isOwner) Some(t)
      else None
    })(identity)

  val ownerTraversal: Traversal[GeneratorTreeF[Tree], Tree] =
    ownerPrism.composeTraversal(regularTraversal[Tree])

  def generatorPrism(gs: Set[Generator]): Prism[GeneratorTreeF[Tree], GeneratorTree] =
    Prism[GeneratorTreeF[Tree], GeneratorTree](t => {
      if (hasGenerator(t.head, gs)) {
        val context = GeneratorInputContext(t.head, getGeneratorsToExpand(t.head, gs))
        Some(Cofree(context, Eval.now(List.empty[GeneratorTree])))
      } else {
        None
      }
    })(_.map(_.src))

  private def hasMatchingGenerator(a: Mod.Annot, gs: Set[Generator]): Boolean =
    gs.exists(isMatching(a, _))

  private def isMatching(a: Mod.Annot, g: Generator) = {
    a.init.tpe match {
      case Type.Name(value) => g.name == value
      case _ => false
    }
  }

  private def hasGenerator(tree: Tree, gs: Set[Generator]): Boolean =
    retrieveAnnotExtractInstance(tree)
      .map(_.extract(tree))
      .exists(_.exists(hasMatchingGenerator(_, gs)))

  private def getGeneratorsToExpand(tree: Tree, gs: Set[Generator]): List[Generator] =
    retrieveAnnotExtractInstance(tree)
      .fold(List[Mod.Annot]())(_.extract(tree))
      .flatMap(annot => gs.find(isMatching(annot, _)))

  def generatorTraversal(
      gs: Set[Generator]): Traversal[GeneratorTreeF[Tree], GeneratorInputContext] =
    ownerPrism
      .composePrism(generatorPrism(gs))
      .composeTraversal(regularTraversal[GeneratorInputContext])

}
