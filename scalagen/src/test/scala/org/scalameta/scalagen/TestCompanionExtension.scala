package org.scalameta.scalagen

import scala.meta._
import scala.meta.contrib._

class TestCompanionExtension extends GeneratorSuite {

  test("Companion extension works with companion absent") {
    val src: Source = source"@PrintHiInCompanion case class Foo()"

    val expected: Source =
      source"""case class Foo()

               object Foo {
                 def hi = println("hi")
               }
             """

    val res = generate(src, PrintHiInCompanion())

    withClue(res.syntax) {
      assert(expected isEqual res)
    }
  }

  test("Companion extension works with companion present") {
    val src: Source =
      source"""@PrintHiInCompanion
               case class Foo()

               object Foo {
                 def foo = ???
               }
             """

    val expected: Source =
      source"""case class Foo()

               object Foo {
                 def foo = ???
                 def hi = println("hi")
               }
             """

    val res = generate(src, PrintHiInCompanion())

    withClue(res.syntax) {
      assert(expected isEqual res)
    }
  }
}
