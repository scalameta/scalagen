package scala.meta.gen.implicits

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
trait Owner {

  // TODO: Complete this list
  implicit class XtensionLogicalParent(t: Tree) {
    def owner: Option[Tree] = {
      t.parent match {
        case Some(p) =>
          p match {
            case b: Member => Some(b)
            case other => other.owner
          }
        case _ => None
      }
    }
  }

}
