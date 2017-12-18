package org.scalameta.scalagen

import scala.meta._
import scala.meta.contrib._

class TestTransmutation extends GeneratorSuite {

  test("Transmutation works") {
    val src: Source =
      source"""@Freeish trait IO {
                  def tell = ???
                  def ask = ???
               }
             """

    val expected: Source =
      source"""sealed trait IO
               case class tell() extends IO
               case class ask() extends IO
             """

    val res = generate(src, Freeish())

    withClue(res.syntax) {
      assert(expected isEqual res)
    }
  }

  test("Deletion is possible") {
    val src: Source =
      source"""@DeleteMe trait IO {
                  def tell = ???
                  def ask = ???
               }
             """

    val expected: Source = source""

    val res = generate(src, DeleteMe())

    withClue(res.syntax) {
      assert(expected isEqual res)
    }
  }
}
