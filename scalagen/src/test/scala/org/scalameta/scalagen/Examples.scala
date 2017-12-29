package org.scalameta.scalagen

import fastparse.core.Logger

import scala.meta._
import scala.meta.contrib._
import scala.meta.gen._

/**
  * Will print the structure of the class as it's toString.
  * Not super useful, but a simple example to test with
  */
case class SyntaxToString() extends ExtensionGenerator("SyntaxToString") {
  override def extend(d: Defn.Class): List[Stat] = {
    val syntax: Lit.String = Lit.String(d.withStats(Nil).syntax)
    val newToString: Defn.Def = q"def toString = $syntax"

    newToString :: Nil
  }
}

case class PrintHi() extends ExtensionGenerator("PrintHi") {
  override def extend(c: Defn.Class): List[Stat] = {
    val hi: Lit.String = Lit.String("hi")
    val hiMethod: Defn.Def =
      q"def hi = println($hi)"

    hiMethod :: Nil
  }
}

case class PrintHiInCompanion() extends CompanionGenerator("PrintHiInCompanion") {
  override def extendCompanion(c: Defn.Class): List[Stat] = {
    val hi: Lit.String = Lit.String("hi")
    val hiMethod: Defn.Def =
      q"def hi = println($hi)"

    hiMethod :: Nil
  }
}

case class TestRecurse() extends ExtensionGenerator("TestRecurse") {
  override def extend(c: Defn.Class): List[Stat] = {
    val clazz = q"@PrintHi class Foo"

    clazz :: Nil
  }
}

case class LogCalls() extends ManipulationGenerator("LogCalls") {
  override def manipulate(d: Defn.Def): Defn.Def = {
    val stats = d.extract[Stat]
    val logger = q"println(${d.name.value + " was called"})"
    d.withStats(logger :: stats)
  }
}

case class Freeish() extends TransmutationGenerator("Freeish") {
  override def transmute(t: Defn.Trait): List[Defn] = {
    val names: List[Term.Name] = t.extract[Stat].collect {
      case d: Defn.Def => d.name
    }

    val subclasses = names.map(_.asType)

    val trat = q"sealed trait ${t.name}"

    val init = Init(t.name, Name.Anonymous(), Nil)
    val template = template"$init"
    val cases = subclasses.map(n => q"case class $n() extends $template")

    trat :: cases
  }
}

case class DeleteMe() extends TransmutationGenerator("DeleteMe") {
  override def transmute(t: Defn.Trait): List[Defn] = Nil
  override def transmute(c: Defn.Class): List[Stat] = Nil
  override def transmute(t: Defn.Type): List[Stat] = Nil
  override def transmute(o: Defn.Object): List[Stat] = Nil
  override def transmute(d: Defn.Def): List[Stat] = Nil
  override def transmute(v: Defn.Val): List[Stat] = Nil
  override def transmute(v: Defn.Var): List[Stat] = Nil
  override def transmute(v: Decl.Var): List[Stat] = Nil
  override def transmute(v: Decl.Val): List[Stat] = Nil
  override def transmute(d: Decl.Def): List[Stat] = Nil
  override def transmute(t: Decl.Type): List[Stat] = Nil
}

case class Abort() extends ExtensionGenerator("Abort") {
  override def extend(c: Defn.Class): List[Stat] =
    abort("Nothing to see here...")
}

case class NonNull() extends ParameterGenerator("NonNull") {
  override def extend(p: Term.Param): List[Stat] = {
    val value = q"assert(${p.name.asTerm} != null)"
    value :: Nil
  }
