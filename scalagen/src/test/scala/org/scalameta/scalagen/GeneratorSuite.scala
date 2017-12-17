package org.scalameta.scalagen

import org.scalameta.scalagen.generators.Generator
import org.scalatest.FunSuite

import scala.meta._

class GeneratorSuite extends FunSuite {

  def generate[A <: Tree](t: A, gs: Generator*): A = {
    Runner(Set(gs: _*)).transform(t)
  }

  def generateRecursive[A <: Tree](t: A, gs: Generator*): A = {
    Runner(Set(gs: _*), recurse = true).transform(t)
  }
}
