package scala.meta.gen.instances

import scala.meta._
import scala.meta.contrib._

//TODO: Move these to scalameta/contrib
trait ExtraInstances {

  type StatReplacer[A] = Replace[A, Stat]
  type StatExtractor[A] = Extract[A, Stat]

  type ModReplacer[A] = Replace[A, Mod]
  type ModExtractor[A] = Extract[A, Mod]

  implicit val extractTermBlockStats: Extract[Term.Block, Stat] =
    Extract(_.stats)

  implicit val replaceTermBlockStats: Replace[Term.Block, Stat] =
    Replace((a, bs) => a.copy(stats = bs))

  implicit val extractPkgStats: Extract[Pkg, Stat] =
    Extract(_.stats)

  implicit val replacePkgStats: Replace[Pkg, Stat] =
    Replace((a, bs) => a.copy(stats = bs))

  implicit val extractSourceStats: Extract[Source, Stat] =
    Extract(_.stats)

  implicit val replaceSourceStats: Replace[Source, Stat] =
    Replace((a, bs) => a.copy(stats = bs))

  implicit val extractTypeMod: Extract[Defn.Type, Mod] =
    Extract(_.mods)

  implicit val replaceTypeMod: Replace[Defn.Type, Mod] =
    Replace((a, bs) => a.copy(mods = bs))

  implicit val extractTypeParamMod: Extract[Type.Param, Mod] =
    Extract(_.mods)

  implicit val replaceTypeParamMod: Replace[Type.Param, Mod] =
    Replace((a, bs) => a.copy(mods = bs))

  implicit val extractTermParamMod: Extract[Term.Param, Mod] =
    Extract(_.mods)

  implicit val replaceTermParamMod: Replace[Term.Param, Mod] =
    Replace((a, bs) => a.copy(mods = bs))

  implicit val extractDeclDefMod: Extract[Decl.Def, Mod] =
    Extract(_.mods)

  implicit val replaceDeclDefMod: Replace[Decl.Def, Mod] =
    Replace((a, bs) => a.copy(mods = bs))

  implicit val extractDeclVarMod: Extract[Decl.Var, Mod] =
    Extract(_.mods)

  implicit val replaceDeclVarMod: Replace[Decl.Var, Mod] =
    Replace((a, bs) => a.copy(mods = bs))

  implicit val extractDeclValMod: Extract[Decl.Val, Mod] =
    Extract(_.mods)

  implicit val replaceDeclValMod: Replace[Decl.Val, Mod] =
    Replace((a, bs) => a.copy(mods = bs))

  implicit val extractDeclTypeMod: Extract[Decl.Type, Mod] =
    Extract(_.mods)

  implicit val replaceDeclTypeMod: Replace[Decl.Type, Mod] =
    Replace((a, bs) => a.copy(mods = bs))
}
