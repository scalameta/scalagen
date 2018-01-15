package scala.meta.gen.implicits

import scala.meta._

/**
  * Designed to produce a tree of logical "stat owners"
  *
  * For example, the parent of something which contains "Stats".
  *
  * Source
  *   Pkg
  *     Defn.Class
  *       Defn.Val
  *         Defn.Def
  *           Defn.Class
  *     Defn.Object
  *       Defn.Var
  *
  * We skip over Template and Term.Block as these are just AST implementation details,
  * and extract/replace typeclasses can easily ignore them
  *
  * Member alone is not good enough.
  * - Source is not a Member
  * - Self is a Member
  *
  * Known currently ignored tree's
  * - Refine
  * - Existential
  * - Ctor.Secondary
  */
trait StatOwner {

  implicit class XtensionStatOwner(t: Tree) {

    /**
      * Whether this tree is indeed an "Owner"
      */
    def isOwner(t: Tree): Boolean =
      t match {
        case _: Source | _: Pkg | _: Defn => true
        case _ => false
      }

    def owner: Option[Tree] =
      t.parent.flatMap {
        case b: Term.Block => b.owner
        case t: Template => t.owner
        case p if isOwner(p) => Some(p)
        case _ => None
      }

    def ownerChildren: List[Tree] =
      t.children.flatMap {
        case b: Term.Block => b.ownerChildren
        case t: Template => t.ownerChildren
        case p if isOwner(p) => List(p)
        case _ => Nil
      }
  }
}
