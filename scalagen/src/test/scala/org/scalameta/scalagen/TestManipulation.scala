package org.scalameta.scalagen

import scala.meta._
import scala.meta.contrib._

class TestManipulation extends GeneratorSuite {

  test("Manipulation works") {
    val deff = q"@LogCalls def foo = 1"

    val expected: Defn.Def =
      q"""def foo = {
          println("foo was called")
          1
        }
       """

    val res = generate(deff, LogCalls())

    withClue(res.syntax) {
      assert(expected isEqual res)
    }
  }
}
