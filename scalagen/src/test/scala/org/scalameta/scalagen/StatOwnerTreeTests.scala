package org.scalameta.scalagen

import org.scalatest.FunSuite

import scala.meta._
import cats._
import cats.implicits._

class StatOwnerTreeTests extends FunSuite {

  test("Single Owner Tree") {
    val src: Member = q"object Foo"

    val expected: String = "Defn.Object"

    val res = OwnerTree(src)
    val str = Show[OwnerTree[Tree]].show(res)

    withClue(str) {
      assert(expected == str)
    }
  }

  test("Two Owner Tree") {
    val src: Member = q"object Foo { def bar = 2 }"

    val expected: String =
      """Defn.Object
        | - Defn.Def""".stripMargin

    val res = OwnerTree(src)
    val str = Show[OwnerTree[Tree]].show(res)

    withClue("Clue:\n\n" + str + "\n\n") {
      assert(expected == str)
    }
  }

  test("Complex Owner Tree") {
    val src: Source =
      source"""package org.scala.meta

               class Baz(a: Int, b: Foo) {
                 def blargh = {
                   val foo = {
                     case object Bar {
                       val foo = new Bar {
                          val baz = () => {
                            val bar = 2
                          }
                       }
                     }
                   }
                 }
               }

               object Foo {
                 def bar = 2
               }"""

    // Note: If we were to support anon classes
    // and functions this would be different
    val expected: String =
      """Source
        | - Pkg
        |   - Defn.Class
        |     - Defn.Def
        |       - Defn.Val
        |         - Defn.Object
        |           - Defn.Val
        |   - Defn.Object
        |     - Defn.Def""".stripMargin

    val res = OwnerTree(src)
    val str = Show[OwnerTree[Tree]].show(res)

    withClue("Clue:\n\n" + str + "\n\n") {
      assert(expected == str)
    }
  }
}
