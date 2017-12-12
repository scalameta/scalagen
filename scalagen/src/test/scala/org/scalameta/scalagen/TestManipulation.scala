package org.scalameta.scalagen

import scala.meta._
import scala.meta.contrib._
import org.scalameta.scalagen.generators._
import org.scalatest._

class TestManipulation extends FunSuite {

  test("Manipulation works") {
    val deff = q"@LogCalls def foo = 1"

    val generator = new LogCalls()
    val runner = ManipulationRunner(Set(generator))

    val out = runner.transform(deff)

    assert(deff.extract[Stat].size === 1)
    assert(out.extract[Stat].size === 2)
    assert(!deff.withMods(Nil).isEqual(out))
  }
}
