package org.scalameta.scalagen

import cats._
import cats.implicits._
import scala.meta._
import scala.meta.gen._

case class OwnerTree[A](value: A, children: List[OwnerTree[A]])

object OwnerTree {

  def apply(t: Tree): OwnerTree[Tree] =
    OwnerTree(t, t.ownerChildren.map(OwnerTree(_)))

  implicit val treeFunctorInstance: Functor[OwnerTree] = new Functor[OwnerTree] {
    override def map[A, B](t: OwnerTree[A])(f: A => B): OwnerTree[B] = {
      OwnerTree(
        f(t.value),
        t.children.map(map(_)(f))
      )
    }
  }

  implicit val treeFoldableInstance: Foldable[OwnerTree] = new Foldable[OwnerTree] {

    override def foldLeft[A, B](fa: OwnerTree[A], b: B)(f: (B, A) => B): B =
      fa.children.foldLeft(f(b, fa.value))((b, a) => f(b, a.value))

    override def foldRight[A, B](t: OwnerTree[A], b: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] =
      f(t.value, t.children.foldRight(b)((t, b) => f(t.value, b)))
  }

  /**
    * Produce a product prefix based owner tree representation
    *
    * Defn.Class
    *  - Defn.Var
    *    - Defn.Def
    */
  implicit def treeShowInstance: Show[OwnerTree[Tree]] =
    Show.show(t => {
      if (t.children.isEmpty) {
        t.value.productPrefix
      } else {
        val childString =
          t.children
            .map(showChildIndented)
            .mkString("\n - ")

        s"""${t.value.productPrefix}
           | - $childString""".stripMargin
      }
    })

  /**
    * Will return the result of Show, indented by 2
    */
  private val showChildIndented: OwnerTree[Tree] => String =
    Show[OwnerTree[Tree]]
      .show(_)
      .lines
      .mkString("\n  ")
}
