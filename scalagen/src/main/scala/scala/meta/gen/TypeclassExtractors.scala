package scala.meta.gen

import scala.meta._
import scala.meta.gen._
import scala.meta.contrib._

/** This is a big hack of a class.
  * We have to retrieve instances at runtime based in input type
  */
// TODO move to scalameta/contrib
object TypeclassExtractors {

  def retrieveModExtractInstance[A](a: A): Option[Extract[A, Mod]] = {
    val res =
      a match {
        case _: Defn.Class => Option(implicitly[Extract[Defn.Class, Mod]])
        case _: Defn.Type => Option(implicitly[Extract[Defn.Type, Mod]])
        case _: Defn.Trait => Option(implicitly[Extract[Defn.Trait, Mod]])
        case _: Defn.Object => Option(implicitly[Extract[Defn.Object, Mod]])
        case _: Defn.Def => Option(implicitly[Extract[Defn.Def, Mod]])
        case _: Defn.Val => Option(implicitly[Extract[Defn.Val, Mod]])
        case _: Defn.Var => Option(implicitly[Extract[Defn.Var, Mod]])
        case _: Decl.Val => Option(implicitly[Extract[Decl.Val, Mod]])
        case _: Decl.Var => Option(implicitly[Extract[Decl.Var, Mod]])
        case _: Decl.Def => Option(implicitly[Extract[Decl.Def, Mod]])
        case _: Decl.Type => Option(implicitly[Extract[Decl.Type, Mod]])
        case _ => None
      }

    res.asInstanceOf[Option[Extract[A, Mod]]]
  }

  def retrieveModReplaceInstance[A](a: A): Option[Replace[A, Mod]] = {
    val res =
      a match {
        case _: Defn.Class => Option(implicitly[Replace[Defn.Class, Mod]])
        case _: Defn.Type => Option(implicitly[Replace[Defn.Type, Mod]])
        case _: Defn.Trait => Option(implicitly[Replace[Defn.Trait, Mod]])
        case _: Defn.Object => Option(implicitly[Replace[Defn.Object, Mod]])
        case _: Defn.Def => Option(implicitly[Replace[Defn.Def, Mod]])
        case _: Defn.Val => Option(implicitly[Replace[Defn.Val, Mod]])
        case _: Defn.Var => Option(implicitly[Replace[Defn.Var, Mod]])
        case _: Decl.Val => Option(implicitly[Replace[Decl.Val, Mod]])
        case _: Decl.Var => Option(implicitly[Replace[Decl.Var, Mod]])
        case _: Decl.Def => Option(implicitly[Replace[Decl.Def, Mod]])
        case _: Decl.Type => Option(implicitly[Replace[Decl.Type, Mod]])
        case _ => None
      }

    res.asInstanceOf[Option[Replace[A, Mod]]]
  }

  def retrieveAnnotExtractInstance[A](a: A): Option[Extract[A, Mod.Annot]] = {
    val res =
      a match {
        case _: Defn.Class => Option(implicitly[Extract[Defn.Class, Mod.Annot]])
        case _: Defn.Type => Option(implicitly[Extract[Defn.Type, Mod.Annot]])
        case _: Defn.Trait => Option(implicitly[Extract[Defn.Trait, Mod.Annot]])
        case _: Defn.Object => Option(implicitly[Extract[Defn.Object, Mod.Annot]])
        case _: Defn.Def => Option(implicitly[Extract[Defn.Def, Mod.Annot]])
        case _: Defn.Val => Option(implicitly[Extract[Defn.Val, Mod.Annot]])
        case _: Defn.Var => Option(implicitly[Extract[Defn.Var, Mod.Annot]])
        case _: Decl.Val => Option(implicitly[Extract[Decl.Val, Mod.Annot]])
        case _: Decl.Var => Option(implicitly[Extract[Decl.Var, Mod.Annot]])
        case _: Decl.Def => Option(implicitly[Extract[Decl.Def, Mod.Annot]])
        case _: Decl.Type => Option(implicitly[Extract[Decl.Type, Mod.Annot]])
        case _ => None
      }

    res.asInstanceOf[Option[Extract[A, Mod.Annot]]]
  }

  def retrieveStatReplaceInstance[A](a: A): Option[Replace[A, Stat]] = {
    val res =
      a match {
        case _: Defn.Class => Option(implicitly[Replace[Defn.Class, Stat]])
        case _: Defn.Trait => Option(implicitly[Replace[Defn.Trait, Stat]])
        case _: Defn.Object => Option(implicitly[Replace[Defn.Object, Stat]])
        case _: Defn.Def => Option(implicitly[Replace[Defn.Def, Stat]])
        case _: Defn.Val => Option(implicitly[Replace[Defn.Val, Stat]])
        case _: Defn.Var => Option(implicitly[Replace[Defn.Var, Stat]])
        case _: Source => Option(implicitly[Replace[Source, Stat]])
        case _: Pkg => Option(implicitly[Replace[Pkg, Stat]])
        case _ => None
      }

    res.asInstanceOf[Option[Replace[A, Stat]]]

  }

  def retrieveStatExtractInstance[A](a: A): Option[Extract[A, Stat]] = {
    val res =
      a match {
        case _: Defn.Class => Option(implicitly[Extract[Defn.Class, Stat]])
        case _: Defn.Trait => Option(implicitly[Extract[Defn.Trait, Stat]])
        case _: Defn.Object => Option(implicitly[Extract[Defn.Object, Stat]])
        case _: Defn.Def => Option(implicitly[Extract[Defn.Def, Stat]])
        case _: Defn.Val => Option(implicitly[Extract[Defn.Val, Stat]])
        case _: Defn.Var => Option(implicitly[Extract[Defn.Var, Stat]])
        case _: Source => Option(implicitly[Extract[Source, Stat]])
        case _: Pkg => Option(implicitly[Extract[Pkg, Stat]])
        case _ => None
      }
    res.asInstanceOf[Option[Extract[A, Stat]]]
  }
}
