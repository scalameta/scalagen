package org.scalameta.scalagen.instances

import scala.meta._
import scala.meta.contrib._

//TODO: Move these to scalameta/contrib
trait ExtraInstances {

  implicit val extractTermBlockStats: Extract[Term.Block, Stat] =
    Extract(_.stats)

  implicit val replaceTermBlockStats: Replace[Term.Block, Stat] =
    Replace((a, bs) => a.copy(stats = bs))
}
