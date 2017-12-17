package org.scalameta.scalagen.implicits

import scala.meta._

/**
  * Find the "parent".
  *
  * This is the user's parent, not the AST's parent.
  *
  * We want to avoid things like Term.Block.
  *
  * We will need to tweak this a lot before it is perfect.
  *
  */
trait LogicalParent {

  // TODO: Complete this list
  implicit class LogicalParent(t: Tree) {
    def logicalParent: Option[Tree] = {
      t.parent match {
        case Some(p) =>
          p match {
            case b: Term.Block => b.logicalParent
            case t: Template => t.logicalParent
            case other => Some(other)
          }
        case _ => None
      }
    }
  }

}
