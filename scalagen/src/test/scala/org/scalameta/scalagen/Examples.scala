package org.scalameta.scalagen

import scala.meta._
import scala.meta.contrib._
import org.scalameta.scalagen.generators._

/**
  * Will print the structure of the class as it's toString.
  * Not super useful, but a simple example to test with
  */
class StructuralToString extends ExtensionGenerator("StructuralToString") {
  override def extend(d: Defn.Class): List[Stat] = {
    val structure: Lit.String = Lit.String(d.withStats(Nil).structure)
    val newToString: Defn.Def = q"def toString = $structure"

    newToString :: Nil
  }
}

class PrintHi extends ExtensionGenerator("PrintHi") {
  override def extend(c: Defn.Class): List[Stat] = {
    val hi: Lit.String = Lit.String("hi")
    val hiMethod: Defn.Def =
      q"def hi = println($hi)"

    hiMethod :: Nil
  }
}

class TestRecurse extends ExtensionGenerator("TestRecurse") {
  override def extend(c: Defn.Class): List[Stat] = {
    val clazz = q"@PrintHi class Foo"

    clazz :: Nil
  }
}
