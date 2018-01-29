package scala.meta.gen

import org.scalatest.FunSuite
import scala.meta._
import scala.meta.gen._
import scala.meta.contrib._
import scala.meta.gen.TypeclassExtractors._

class TypeclassExtractorTest extends FunSuite {

  testModInstanceRetrieval(q"class Foo")
  testModInstanceRetrieval(q"object Foo")
  testModInstanceRetrieval(q"trait Foo")
  testModInstanceRetrieval(q"def foo")
  testModInstanceRetrieval(q"type Foo")
  testModInstanceRetrieval(q"val foo: Int")
  testModInstanceRetrieval(q"var foo: Int")
  testModInstanceRetrieval(q"def foo = 1")
  testModInstanceRetrieval(q"type Foo = Int")
  testModInstanceRetrieval(q"val foo = 1")
  testModInstanceRetrieval(q"var foo = 1")

  testStatInstanceRetrieval(q"class Foo")
  testStatInstanceRetrieval(q"object Foo")
  testStatInstanceRetrieval(q"trait Foo")
  testStatInstanceRetrieval(q"def foo = 1")
  testStatInstanceRetrieval(q"val foo = 1")
  testStatInstanceRetrieval(q"var foo = 1")
  testStatInstanceRetrieval(source"object foo")
  testStatInstanceRetrieval(source"package foo; object foo".children.head)

  private def testModInstanceRetrieval(t: Tree) = {
    test(s"Retrieve mod extract instance: ${t.syntax}") {
      assert(retrieveModExtractInstance(t).isDefined)
    }
    test(s"Retrieve mod replace instance: ${t.syntax}") {
      assert(retrieveModReplaceInstance(t).isDefined)
    }
  }

  private def testStatInstanceRetrieval(t: Tree) = {
    test(s"Retrieve stat extract instance: ${t.syntax}") {
      assert(retrieveStatExtractInstance(t).isDefined)
    }
    test(s"Retrieve stat replace instance: ${t.syntax}") {
      assert(retrieveStatReplaceInstance(t).isDefined)
    }
  }
}
