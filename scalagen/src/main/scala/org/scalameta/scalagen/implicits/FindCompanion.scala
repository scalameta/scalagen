package org.scalameta.scalagen.implicits

import scala.meta._
import scala.meta.contrib._

/**
  * For the purposes of scalagen,
  *
  * A companion is a Defn.Object with a Term.Name which is equal to that of some other Definition's Term.Type.
  *
  * This means the following are valid
  *
  * - Defn.Type
  * - Defn.Trait
  * - Defn.Class
  *
  * Note: Cant use tree.find, as we must search at the correct level.
  */
trait FindCompanion {

  private def findCompanion(name: String, t: Tree): Option[Defn.Object] = {
    t.parent match {
      case Some(b: Term.Block) => findObjectWithName(name, b)
      case Some(templ: Template) => findObjectWithName(name, templ)
      case Some(src: Source) => findObjectWithName(name, src)
      case Some(pkg: Pkg) => findObjectWithName(name, pkg)
      case _ => None
    }
  }

  private def findObjectWithName[A <: Tree](name: String, t: A)(
      implicit ev: Extract[A, Stat]): Option[Defn.Object] = {
    t.extract[Stat].collectFirst {
      case o: Defn.Object if o.name.value == name => o
    }
  }

  implicit class XtensionFindCompanionClass(c: Defn.Class) {
    def companionObject: Option[Defn.Object] = findCompanion(c.name.value, c)
  }

  implicit class XtensionFindCompanionType(t: Defn.Type) {
    def companionObject: Option[Defn.Object] = findCompanion(t.name.value, t)
  }

  implicit class XtensionFindCompanionTrait(t: Defn.Trait) {
    def companionObject: Option[Defn.Object] = findCompanion(t.name.value, t)
  }
}
