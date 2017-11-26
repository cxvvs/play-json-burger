package org.cxvvs.playjson

import org.cxvvs.playjson.macros.JsonFormat
import org.scalatest.FlatSpec
import play.api.libs.json.{JsSuccess, Json, OFormat, Reads}

class MacroSpec extends FlatSpec {
  @JsonFormat
  case class Testing(a: Int, b: String)
  object Testing {
    implicit val format: OFormat[Testing] = macroFormat
  }

  "Format Macro" should "correctly read a simple case class" in {
    // Given
    val value = Testing(42, "Mario")
    val jsonValue = Json.obj("a" -> 42, "b" -> "Mario")
    val testingReads: Reads[Testing] = implicitly[Reads[Testing]]

    // When
    val readValue = testingReads.reads(jsonValue)

    // Then
    assert(readValue == JsSuccess(value))
  }
}
