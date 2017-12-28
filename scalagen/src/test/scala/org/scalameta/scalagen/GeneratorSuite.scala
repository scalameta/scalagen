package org.scalameta.scalagen

import org.scalatest.FunSuite

import scala.meta._
import scala.meta.gen.Generator

class GeneratorSuite extends FunSuite {

  def generate[A <: Tree](t: A, gs: Generator*): A = {
    Runner(Set(gs: _*)).transform(t)
  }

  def generateRecursive[A <: Tree](t: A, gs: Generator*): A = {
    Runner(Set(gs: _*), recurse = true).transform(t)
  }
}
