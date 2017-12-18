package org.scalameta.scalagen.instances

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
}
