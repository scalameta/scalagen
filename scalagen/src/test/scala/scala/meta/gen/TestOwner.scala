package scala.meta.gen

import org.scalatest.FunSuite

import scala.meta._
import scala.meta.contrib._

// TODO: Add lots more tests
class TestOwner extends FunSuite {

  test("Owner works in class") {
    val clazz: Defn.Class =
      q"""class Foo {
           def bar = baz
           val bar = baz
           var bar = baz
           class Foo
           trait Foo
           type Foo
           object Bar
         }"""

    assert(clazz.extract[Stat].forall(_.owner.contains(clazz)))
  }

  test("Owner works in def") {
    val deff: Defn.Def =
      q"""def foo = {
           def bar = baz
           val bar = baz
           var bar = baz
           class Foo
           trait Foo
           object Bar
         }"""

    assert(deff.extract[Stat].forall(_.owner.contains(deff)))
  }

  test("Owner works in Source") {
    val source: Source =
      source"""
           class Foo
           trait Foo
           object Bar
         """

    assert(source.extract[Stat].forall(_.owner.contains(source)))
  }

  test("Owner works in Package") {
    val pkg: Pkg =
      q""" package foo.bar {
              class Foo
              trait Foo
              object Bar
           }
         """

    assert(pkg.extract[Stat].forall(_.owner.contains(pkg)))
  }

  test("Owner children Pkg") {
    val pkg: Pkg =
      q""" package foo.bar {
              class Foo
              trait Foo
              object Bar
           }
         """

    assert(pkg.ownerChildren.size == 3)
  }

  test("Owner children in Source") {
    val source: Source =
      source"""
           class Foo
           trait Foo
           object Bar
         """

    assert(source.ownerChildren.size == 3)
  }

  test("Owner children works in Class") {
    val clazz: Defn.Class =
      q"""class Foo {
           assert(1 == 2)
           def bar = baz
           val bar = baz
           var bar = baz
           class Foo
           trait Foo
           type Foo
           object Bar
         }"""

    assert(clazz.ownerChildren.size == 6)
  }

  test("Owner children works in Def") {
    val deff: Defn.Def =
      q"""def foo = {
           def bar = baz
           val bar = baz
           var bar = baz
           class Foo
           trait Foo
           object Bar
           2
         }"""

    assert(deff.ownerChildren.size == 6)
  }
}
