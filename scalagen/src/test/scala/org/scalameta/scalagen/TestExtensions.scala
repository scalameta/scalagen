package org.scalameta.scalagen

import scala.meta._
import scala.meta.contrib._
import org.scalameta.scalagen.generators._
import org.scalatest._

class TestExtensions extends FunSuite {

  test("Expansion works") {
    val clazz: Defn.Class = q"@StructuralToString case class Foo(x: Int, y: Int)"
    val generator = new StructuralToString()
    val runner = Runner(Set(generator))

    val out: Defn.Class = runner.transform(clazz)

    assert(clazz.extract[Stat].size === 0)
    assert(out.extract[Stat].size === 1)
    assert(!clazz.withMods(Nil).isEqual(out))
  }

  test("Mutliple Expansion works") {
    val clazz: Defn.Class =
      q"@PrintHi @StructuralToString case class Foo(x: Int, y: Int)"
    val generators: Set[Generator] = Set(new StructuralToString(), new PrintHi())
    val runner = Runner(generators)

    val out: Defn.Class = runner.transform(clazz)

    assert(clazz.extract[Stat].size === 0)
    assert(out.extract[Stat].size === 2)
    assert(!clazz.withMods(Nil).isEqual(out))
  }

  test("Duplicate Expansion works") {
    val clazz: Defn.Class =
      q"@PrintHi @PrintHi case class Foo(x: Int, y: Int)"
    val generators: Set[Generator] = Set(new StructuralToString(), new PrintHi())
    val runner = Runner(generators)

    val out: Defn.Class = runner.transform(clazz)

    assert(clazz.extract[Stat].size === 0)
    assert(out.extract[Stat].size === 2)
    assert(!clazz.withMods(Nil).isEqual(out))
  }

  test("Annotations are removed") {
    val clazz: Defn.Class =
      q"@PrintHi case class Foo(x: Int, y: Int)"
    val generators: Set[Generator] = Set(new PrintHi())
    val runner = Runner(generators)

    val out: Defn.Class = runner.transform(clazz)

    assert(out.extract[Mod].size == 1)
  }

  ignore("Recursion Disabled by default") {
    val clazz: Defn.Class =
      q"@TestRecurse case class Foo(x: Int, y: Int)"
    val generators: Set[Generator] =
      Set(new StructuralToString(), new PrintHi(), new TestRecurse())
    val runner = Runner(generators, recurse = true)

    val out: Defn.Class = runner.transform(clazz)

    assert(clazz.extract[Stat].size === 0)
    assert(out.extract[Stat].size === 1)
    assert(!clazz.withMods(Nil).isEqual(out))
    assert(out.extract[Stat].head.asInstanceOf[Defn.Class].extract[Stat].isEmpty)
  }

  // Fails due to traversal being pre-order, thus the outer is generated and
  // Then the tranformation travels over children.
  test("Recursion works") {
    val clazz: Defn.Class =
      q"@TestRecurse case class Foo(x: Int, y: Int)"
    val generators: Set[Generator] =
      Set(new StructuralToString(), new PrintHi(), new TestRecurse())
    val runner = Runner(generators, recurse = true)

    val out: Defn.Class = runner.transform(clazz)

    assert(clazz.extract[Stat].size === 0)
    assert(out.extract[Stat].size === 1)
    assert(!clazz.withMods(Nil).isEqual(out))
    val inner = out.extract[Stat].head.asInstanceOf[Defn.Class]
    assert(inner.extract[Stat].size == 1)
  }

  test("Nested Expansion works") {
    val inner: Defn.Class = q"@StructuralToString case class Bar(y: Int)"

    val clazz: Defn.Class =
      q"@StructuralToString case class Foo(x: Int, y: Int) { $inner }"
    val generator = new StructuralToString()
    val runner = Runner(Set(generator))

    val out: Defn.Class = runner.transform(clazz)

    assert(clazz.extract[Stat].size === 1)
    assert(out.extract[Stat].size === 2)
    val outInner = out.extract[Stat].head.asInstanceOf[Defn.Class]

    assert(outInner.extract[Stat].size === 1)
  }

  test("Expansion noop") {
    val clazz: Defn.Class = q"case class Foo(x: Int, y: Int)"
    val generator = new StructuralToString()
    val runner = Runner(Set(generator))

    val out: Defn.Class = runner.transform(clazz)

    assert(clazz.extract[Stat].isEmpty)
    assert(out.extract[Stat].isEmpty)
    assert(clazz isEqual out)
  }
}
