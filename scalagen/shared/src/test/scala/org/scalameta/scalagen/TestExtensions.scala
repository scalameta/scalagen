package org.scalameta.scalagen

import scala.meta._
import scala.meta.contrib._
import org.scalameta.scalagen.generators._
import org.scalatest._

class TestExtensions extends FunSuite {

  test("Expansion works") {
    val clazz: Defn.Class = q"@StructuralToString case class Foo(x: Int, y: Int)"
    val generator = new StructuralToString()
    val runner = ExtensionRunner(Set(generator))

    val out: Defn.Class = runner.transform(clazz)

    assert(clazz.extract[Stat].size === 0)
    assert(out.extract[Stat].size === 1)
    assert(!clazz.isEqual(out))
  }

  test("Mutliple Expansion works") {
    val clazz: Defn.Class =
      q"@PrintHi @StructuralToString case class Foo(x: Int, y: Int)"
    val generators: Set[Generator] = Set(new StructuralToString(), new PrintHi())
    val runner = ExtensionRunner(generators)

    val out: Defn.Class = runner.transform(clazz)

    assert(clazz.extract[Stat].size === 0)
    assert(out.extract[Stat].size === 2)
    assert(!clazz.isEqual(out))
  }

  test("Nested Expansion works") {
    val inner: Defn.Class = q"@StructuralToString case class Bar(y: Int)"

    val clazz: Defn.Class =
      q"@StructuralToString case class Foo(x: Int, y: Int) { $inner }"
    val generator = new StructuralToString()
    val runner = ExtensionRunner(Set(generator))

    val out: Defn.Class = runner.transform(clazz)

    assert(clazz.extract[Stat].size === 1)
    assert(out.extract[Stat].size === 2)
    val outInner = out.extract[Stat].head.asInstanceOf[Defn.Class]

    assert(outInner.extract[Stat].size === 1)
    assert(outInner.withStats(Nil) isEqual inner)
    assert(!clazz.isEqual(out))
  }

  test("Expansion noop") {
    val clazz: Defn.Class = q"case class Foo(x: Int, y: Int)"
    val generator = new StructuralToString()
    val runner = ExtensionRunner(Set(generator))

    val out: Defn.Class = runner.transform(clazz)

    assert(clazz.extract[Stat].isEmpty)
    assert(out.extract[Stat].isEmpty)
    assert(clazz isEqual out)
  }
}
