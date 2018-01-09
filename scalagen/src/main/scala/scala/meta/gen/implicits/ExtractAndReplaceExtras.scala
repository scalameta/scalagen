package scala.meta.gen.implicits

import scala.meta.contrib._

trait ExtractAndReplaceExtras {

  implicit class XtensionExtractAndReplace[A](a: A) {
    def prepend[B](bs: List[B])(implicit rev: Replace[A, B], eev: Extract[A, B]): A =
      replaceShortCircuited(bs ::: eev.extract(a))

    def replaceShortCircuited[B](bs: List[B])(implicit rev: Replace[A, B], eev: Extract[A, B]): A =
      if (eev.extract(a) eq bs) {
        a
      } else {
        rev.replace(a, bs)
      }
  }
}