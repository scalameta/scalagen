package org.scalameta.scalagen

import org.scalatest.FunSuite

import scala.meta._
import scala.meta.contrib._
import org.scalameta.scalagen.implicits._

// TODO: Add lots more tests
class TestLogicalParent extends FunSuite {

  test("Logical parent works") {
    val clazz: Defn.Class = q"class Foo { def bar }"

    assert(clazz.extract[Stat].head.logicalParent.contains(clazz))
  }

}
