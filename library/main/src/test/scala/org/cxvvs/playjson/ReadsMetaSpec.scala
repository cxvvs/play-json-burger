package org.cxvvs.playjson

import org.scalatest._
import org.scalatest.prop.Checkers
import play.api.libs.json._
import shapeless.test.illTyped

class ReadsMetaSpec extends FlatSpec with Checkers with Matchers {
  case class TestClass(a: Int, b: String, c: Double, d: Option[String])

  val testClassMacroFormat: Reads[TestClass] = Json.reads[TestClass]

  val testClassFormatMeta: Reads[TestClass] = {
    import Implicits._
    ReadsMeta[TestClass].from(
      (__ \ "a").read[Int] and
      (__ \ "b").read[String] and
      (__ \ "c").read[Double] and
      (__ \ "d").readNullable[String]
    )
  }

  "ReadsMeta" should "yield the same read results as the play json macro" in {
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

  it should "not compile when building with non matching format types" in {
    // A Format[String] is given instead of a Format[Double] for 'c' here
    illTyped("""
      import Implicits._
      ReadsMeta[TestClass].from(
        (__ \ "a").read[Int] and
          (__ \ "b").read[String] and
          (__ \ "c").read[String] and
          (__ \ "d").readNullable[String]
      )
    """)
  }

  it should "not compile when building with non missing format types" in {
    // 'd' is missing
    illTyped("""
      import Implicits._
      ReadsMeta[TestClass].from(
        (__ \ "a").read[Int] and
          (__ \ "b").read[String] and
          (__ \ "c").read[String]
      )
    """)
  }
}
