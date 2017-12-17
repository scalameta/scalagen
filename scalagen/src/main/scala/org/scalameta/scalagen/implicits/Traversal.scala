package org.scalameta.scalagen.implicits

import scala.meta._

trait Traversal {

  implicit class LeafFirstTraversal(t: Tree) {
    def leafFirstTransform(f: PartialFunction[Tree, Tree]): Tree = {
      object transformer extends Transformer {
        override def apply(tree: Tree): Tree = {
          if (f.isDefinedAt(tree)) f(super.apply(tree))
          else super.apply(tree)
        }
      }

      transformer(t)
    }
  }
}
