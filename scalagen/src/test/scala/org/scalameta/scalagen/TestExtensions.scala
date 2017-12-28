package org.scalameta.scalagen

import scala.meta._
import scala.meta.contrib._

class TestExtensions extends GeneratorSuite {

  test("Expansion works") {
    val clazz: Defn.Class =
      q"@SyntaxToString case class Foo(x: Int, y: Int)"

    val expected: Defn.Class =
      q"""case class Foo(x: Int, y: Int) {
            def toString = "@SyntaxToString case class Foo(x: Int, y: Int)"
          }
       """

    val res = generate(clazz, SyntaxToString())

    withClue(res.syntax) {
      assert(expected isEqual res)
    }
  }

  test("Multiple Expansion works") {
    val clazz: Defn.Class =
      q"@PrintHi @SyntaxToString case class Foo(x: Int, y: Int)"

    val expected: Defn.Class =
      q"""case class Foo(x: Int, y: Int) {
          def hi = println("hi")
          def toString = "@SyntaxToString case class Foo(x: Int, y: Int)"
        }
       """

    val res = generate(clazz, SyntaxToString(), PrintHi())

    withClue(res.syntax) {
      assert(expected isEqual res)
    }
  }

  test("Duplicate Expansion works") {
    val clazz: Defn.Class =
      q"@PrintHi @PrintHi case class Foo(x: Int, y: Int)"

    val expected: Defn.Class =
      q"""case class Foo(x: Int, y: Int) {
          def hi = println("hi")
          def hi = println("hi")
        }
       """

    val res = generate(clazz, PrintHi())

    withClue(res.syntax) {
      assert(expected isEqual res)
    }
  }

  test("Recursion Disabled by default") {
    val clazz: Defn.Class =
      q"@TestRecurse case class Foo(x: Int, y: Int)"

    val expected: Defn.Class =
      q"""case class Foo(x: Int, y: Int) {
            @PrintHi class Foo
        }
       """

    val res = generate(clazz, PrintHi(), TestRecurse())

    withClue(res.syntax) {
      assert(expected isEqual res)
    }
  }

  test("Recursion works") {
    val clazz: Defn.Class =
      q"@TestRecurse case class Foo(x: Int, y: Int)"

    val expected: Defn.Class =
      q"""case class Foo(x: Int, y: Int) {
            class Foo {
              def hi = println("hi")
            }
        }
       """

    val res = generateRecursive(clazz, PrintHi(), TestRecurse())

    withClue(res.syntax) {
      assert(expected isEqual res)
    }
  }

  test("Nested Expansion works") {
    val inner: Defn.Class = q"@PrintHi case class Bar(y: Int)"

    val clazz: Defn.Class =
      q"@PrintHi case class Foo(x: Int, y: Int) { $inner }"

    val expected: Defn.Class =
      q"""case class Foo(x: Int, y: Int) {
            case class Bar(y: Int) {
              def hi = println("hi")
            }

          def hi = println("hi")
        }
       """

    val res = generate(clazz, PrintHi(), TestRecurse())

    withClue(res.syntax) {
      assert(expected isEqual res)
    }
  }

  test("Expansion noop") {
    val clazz: Defn.Class = q"case class Foo(x: Int, y: Int)"
    val res = generate(clazz, PrintHi(), TestRecurse())

    withClue(res.syntax) {
      assert(clazz isEqual res)
    }
  }
}
