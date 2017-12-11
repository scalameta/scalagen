package org.scalameta.scalagen

import scala.meta._
import scala.meta.contrib._
import org.scalameta.scalagen._
import org.scalameta.scalagen.generators._
import org.scalatest._

class TestExtensions extends FunSuite {

  test("Expansion works") {
    val clazz: Defn.Class = q"@StructuralToString case class Foo(x: Int, y: Int)"
    val generator = new StructuralToString()
    val runner = Runner(clazz, Set(generator))

    val out: Defn.Class = runner.transform

    assert(clazz.extract[Stat].size === 0)
    assert(out.extract[Stat].size === 1)
    assert(!clazz.isEqual(out))
  }

  test("Expansion noop") {
    val clazz: Defn.Class = q"case class Foo(x: Int, y: Int)"
    val generator = new StructuralToString()
    val runner = Runner(clazz, Set(generator))

    val out: Defn.Class = runner.transform

    assert(clazz.extract[Stat].isEmpty)
    assert(out.extract[Stat].isEmpty)
    assert(clazz isEqual out)
  }
}
