package org.scalameta.scalagen

import org.scalatest.FunSuite
import scala.meta._
import scala.meta.contrib._
import org.scalameta.scalagen.implicits._

class TestFindCompanion extends FunSuite {

  test("Companion found at top level") {
    val clazz = q"case class Bar()"
    val obj = q"object Bar"
    val source =
      source"""
        package scala.meta.foo

        $clazz

        $obj
      """

    // Cant use class directly because it wont have a parent
    val clazzz: Defn.Class = source.find(_ isEqual clazz).get.asInstanceOf[Defn.Class]

    assert(clazzz.companionObject.exists(_ isEqual obj))
  }

  test("Companion found in Object") {
    val clazz = q"case class Bar()"
    val obj = q"object Bar"
    val source =
      source"""
        package scala.meta.foo

        object foo {
          $clazz

          $obj
         }
      """

    // Cant use class directly because it wont have a parent
    val clazzz: Defn.Class = source.find(_ isEqual clazz).get.asInstanceOf[Defn.Class]

    assert(clazzz.companionObject.exists(_ isEqual obj))
  }

  test("Companion found in def") {
    val clazz = q"case class Bar()"
    val obj = q"object Bar"
    val source =
      source"""
        package scala.meta.foo

        object Foo {
          def foo = {
            $clazz

            $obj
           }
         }
      """

    // Cant use class directly because it wont have a parent
    val clazzz: Defn.Class = source.find(_ isEqual clazz).get.asInstanceOf[Defn.Class]

    assert(clazzz.companionObject.exists(_ isEqual obj))
  }
}
