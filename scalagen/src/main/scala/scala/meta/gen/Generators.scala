package scala.meta.gen

import scala.annotation.StaticAnnotation
import scala.meta._

trait Generator extends StaticAnnotation {
  def name: String
}

/**
  * Use this trait for extending existing definitions.
  *
  * Example use case: Generating methods.
  *
  * Default: Add no new stats
  */
abstract class ExtensionGenerator(val name: String) extends Generator {
  def extend(c: Defn.Class): List[Stat] = Nil
  def extend(t: Defn.Trait): List[Stat] = Nil
  def extend(o: Defn.Object): List[Stat] = Nil
}

object IdentityGenerator extends ExtensionGenerator("Identity")

/**
  * Use this trait for extending the case class of a Defn.
  *
  * Example use case: Deriving typeclass instances.
  *
  * Default: Add no new stats
  *
  * Note: These *will* generate a companion if one does not exist.
  */
abstract class CompanionGenerator(val name: String) extends Generator {
  def extendCompanion(c: Defn.Class): List[Stat] = Nil
  def extendCompanion(c: Defn.Type): List[Stat] = Nil
  def extendCompanion(c: Defn.Trait): List[Stat] = Nil
}

/**
  * Use this trait when you want full control
  * over the annotee, but still remain in a
  * blackbox style
  *
  * Example use case: Decorators
  *
  * Default: return the input
  */
abstract class ManipulationGenerator(val name: String) extends Generator {
  def manipulate(c: Defn.Class): Defn.Class = c
  def manipulate(t: Defn.Trait): Defn.Trait = t
  def manipulate(o: Defn.Object): Defn.Object = o
  def manipulate(d: Defn.Def): Defn.Def = d
  def manipulate(v: Defn.Val): Defn.Val = v
  def manipulate(v: Defn.Var): Defn.Var = v
}

/**
  * Black magic
  *
  * Use this when you want to generate a number of
  * Definitions inside your parent, or even remove the original
  *
  * This is not guaranteed to work for all cases.
  *
  * For example, generating a top level `Val` will be invalid
  *
  * Default: return the input
  */
abstract class TransmutationGenerator(val name: String) extends Generator {
  def transmute(c: Defn.Class): List[Defn] = c :: Nil
  def transmute(t: Defn.Trait): List[Defn] = t :: Nil
  def transmute(t: Defn.Type): List[Defn] = t :: Nil
  def transmute(o: Defn.Object): List[Defn] = o :: Nil
  def transmute(d: Defn.Def): List[Defn] = d :: Nil
  def transmute(v: Defn.Val): List[Defn] = v :: Nil
  def transmute(v: Defn.Var): List[Defn] = v :: Nil
}
