package org.cxvvs.playjson

import org.scalatest._
import org.scalatest.prop.Checkers
import play.api.libs.json.{JsError, JsSuccess, Json, OFormat, __}
import shapeless.test.illTyped

class FormatMetaSpec extends FlatSpec with Checkers with Matchers {
  case class TestClass(a: Int, b: String, c: Double, d: Option[String])

  val testClassMacroFormat: OFormat[TestClass] = Json.format[TestClass]

  val testClassFormatMeta: OFormat[TestClass] = {
    import Implicits._
    FormatMeta[TestClass].from(
      (__ \ "a").format[Int] and
        (__ \ "b").format[String] and
        (__ \ "c").format[Double] and
        (__ \ "d").formatNullable[String]
    )
  }

  "FormatMeta" should "yield the same read results as the play json macro" in {
    val macroReads = testClassMacroFormat.reads _
    val metaReads = testClassFormatMeta.reads _

    check {
      (a: Int, b: String, c: Either[String, Double], d: Option[String]) =>
        val jsValue = Json.obj(
          "a" -> a,
          "b" -> b
        ) ++ (
          c match {
            case Left(cString) => Json.obj("c" -> cString)
            case Right(cDouble) => Json.obj("c" -> cDouble)
          }
        ) ++ (
          d.map(dd => Json.obj("d" -> dd)).getOrElse(Json.obj())
        )

        (macroReads(jsValue), metaReads(jsValue)) match {
          case (JsSuccess(value1, _), JsSuccess(value2, _)) => value1 == value2
          case (JsError(macroErrors), JsError(metaErrors)) =>
            macroErrors.toSet == metaErrors.toSet
          case _ => false
        }
    }
  }

  it should "yield the same write results as the play json macro" in {
    val macroWrites = testClassMacroFormat.writes _
    val metaWrites = testClassFormatMeta.writes _

    check { (a: Int, b: String, c: Double, d: Option[String]) =>
      val testClass = TestClass(a, b, c, d)
      macroWrites(testClass) == metaWrites(testClass)
    }
  }

  it should "not compile when building with non matching format types" in {
    // A Format[String] is given instead of a Format[Double] for 'c' here
    illTyped("""
      import Implicits._
      FormatMeta[TestClass].from(
        (__ \ "a").format[Int] and
          (__ \ "b").format[String] and
          (__ \ "c").format[String] and
          (__ \ "d").formatNullable[String]
      )
    """)
  }

  it should "not compile when building with non missing format types" in {
    // 'd' is missing
    illTyped("""
      import Implicits._
      FormatMeta[TestClass].from(
        (__ \ "a").format[Int] and
          (__ \ "b").format[String] and
          (__ \ "c").format[String]
      )
    """)
  }
}
