import scala.meta._
import scala.meta.contrib._
import scala.meta.gen._

object Generators {

  object Main extends ManipulationGenerator("Main") {
    override def manipulate(o: Defn.Object): Defn.Object = {
      val stats = o.extract[Stat]

      val main: Stat = q"def main(args: Array[String]): Unit = { ..$stats }"

      o.withStats(main :: Nil)
    }
  }
}