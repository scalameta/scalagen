package org.scalameta.scalagen

import scala.meta._
import scala.meta.contrib._

class TestParameterGenerators extends GeneratorSuite {
  ignore("Parameter generators work") {
    val clazz: Defn.Class =
      q"case class Foo(@NonNull x: Int, @NonNull y: Int)"

    val expected: Defn.Class =
      q"""case class Foo(x: Int, y: Int) {
            assert(x != null)
            assert(y != null)
          }
       """

    val res = generate(clazz, NonNull)

    withClue(res.syntax) {
      assert(expected isEqual res)
    }
  }
}
