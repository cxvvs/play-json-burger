package org.cxvvs.playjson

import org.cxvvs.playjson.macros.JsonFormat
import org.scalatest.FlatSpec
import org.scalatest.prop.Checkers
import play.api.libs.json.{JsSuccess, Json, OFormat, Reads}

class MacroSpec extends FlatSpec with Checkers {

  "Format Macro" should "correctly read a simple case class" in {
    // Given
    @JsonFormat
    case class Testing(a: Int, b: String)
    object Testing {
      implicit val format: OFormat[Testing] = macroFormat
    }
    val value = Testing(42, "Mario")
    val jsonValue = Json.obj("a" -> 42, "b" -> "Mario")
    val testingReads: Reads[Testing] = implicitly[Reads[Testing]]

    // When
    val readValue = testingReads.reads(jsonValue)

    // Then
    assert(readValue == JsSuccess(value))
  }

  "Format Macro" should "correctly handle optional fields" in {
    // Given
    @JsonFormat
    case class Testing(a: Int, b: String, c: Option[Int])
    object Testing {
      implicit val format: OFormat[Testing] = macroFormat
    }

    check {
      (a: Int, b: String, c: Option[Int]) =>
        val value = Testing(a, b, c)
        val jsonValue = Json.obj("a" -> a, "b" -> b) ++ c.map(cValue => Json.obj("c" -> c)).getOrElse(Json.obj())
        val testingReads: Reads[Testing] = implicitly[Reads[Testing]]

        // When
        val readValue = testingReads.reads(jsonValue)

        // Then
        readValue == JsSuccess(value)
    }
  }
}
