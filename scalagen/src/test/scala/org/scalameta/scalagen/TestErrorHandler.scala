package org.scalameta.scalagen

import scala.meta._
import scala.meta.gen.FatalGenerationException

class TestErrorHandler extends GeneratorSuite {

  test("Fatal Exception") {
    val clazz: Defn.Class =
      q"@Abort case class Foo(x: Int, y: Int)"

    assertThrows[FatalGenerationException] {
      generate(clazz, Abort())
    }
  }

}
