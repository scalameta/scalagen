package org.scalameta.scalagen

import org.scalatest.FunSuite
import scala.meta._
import scala.meta.contrib._
import scala.meta.gen._

class TestTraversalOrder extends FunSuite {

  test("Companions are reordered") {
    val in =
      source"""
         class Foo
         object Foo
         object Bar
         class Bar
         trait Bar
         object Bax
      """

    val expected =
      source"""
         object Foo
         object Bar
         object Bax
         class Foo
         class Bar
         trait Bar
      """

    val res = in.withStats(in.extract[Stat].sortWith(Runner.statOrdering))

    withClue(res.syntax) {
      assert(res isEqual expected)
    }
  }
}
