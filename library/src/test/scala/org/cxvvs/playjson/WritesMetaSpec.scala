package org.cxvvs.playjson

import org.scalatest._
import org.scalatest.prop.Checkers
import play.api.libs.json._

class WritesMetaSpec extends FlatSpec with Checkers with Matchers {
  case class TestClass(a: Int, b: String, c: Double, d: Option[String])

  val testClassMacroFormat: OWrites[TestClass] = Json.writes[TestClass]

  val testClassFormatMeta: OWrites[TestClass] = {
    import Implicits._
    WritesMeta[TestClass].from(
      (__ \ "a").write[Int] and
      (__ \ "b").write[String] and
      (__ \ "c").write[Double] and
      (__ \ "d").writeNullable[String]
    )
  }

  "WritesMeta" should "yield the same write results as the play json macro" in {
    val macroWrites = testClassMacroFormat.writes _
    val metaWrites = testClassFormatMeta.writes _

    check { (a: Int, b: String, c: Double, d: Option[String]) =>
      val testClass = TestClass(a, b, c, d)
      macroWrites(testClass) == metaWrites(testClass)
    }
  }
}
