package org.cxvvs.playjson

import org.cxvvs.playjson.macros.JsonFormat
import org.scalatest.FlatSpec
import org.scalatest.prop.Checkers
import play.api.libs.json._

class MacroSpec extends FlatSpec with Checkers {

  "Format Macro" should "correctly read a simple case class" in {
    // Given
    @JsonFormat
    case class Testing(a: Int, b: String)
    object Testing {
      implicit val format: OFormat[Testing] = defaultFormat.build
    }

    check {
      (a: Int, b: String) =>
        val value = Testing(a, b)
        val jsonValue = Json.obj("a" -> a, "b" -> b)
        val testingReads: Reads[Testing] = implicitly[Reads[Testing]]

        // When
        val readValue = testingReads.reads(jsonValue)

        // Then
        readValue == JsSuccess(value)
    }
  }

  it should "correctly handle optional fields" in {
    // Given
    @JsonFormat
    case class Testing(a: Int, b: String, c: Option[Int])
    object Testing {
      implicit val format: OFormat[Testing] = defaultFormat.build
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

  it should "support adding arbitrary constraints on mandatory fields" in {
    // Given
    @JsonFormat
    case class Testing(a: Int, b: String)
    object Testing {
      val macroFormat: OFormat[Testing] = defaultFormat
        .bRead(Reads.minLength[String](4))
        .build

      val handFormat: OFormat[Testing] = {
        import play.api.libs.functional.syntax._
        import play.api.libs.json._
        OFormat(
          (
            (__ \ "a").read[Int] and
            (__ \ "b").read[String](Reads.minLength[String](4))
          )(Testing.apply _),
          (
            (__ \ "a").write[Int] and
            (__ \ "b").write[String]
          )(unlift(Testing.unapply))
        )
      }
    }

    check {
      (a: Int, b: String, c: Option[Int]) =>
        val value = Testing(a, b)
        val jsonValue = Json.obj("a" -> a, "b" -> b)

        // When
        val macroRead = Testing.macroFormat.reads(jsonValue)
        val handRead = Testing.handFormat.reads(jsonValue)

        // Then
        macroRead == handRead
    }
  }

  it should "support adding arbitrary constraints on optional fields" in {
    // Given
    @JsonFormat
    case class Testing(a: Int, b: Option[String])
    object Testing {
      val macroFormat: OFormat[Testing] = defaultFormat
        .bRead(Reads.minLength[String](4))
        .build

      val handFormat: OFormat[Testing] = {
        import play.api.libs.functional.syntax._
        import play.api.libs.json._
        OFormat(
          (
            (__ \ "a").read[Int] and
            (__ \ "b").readNullable[String](Reads.minLength[String](4))
            )(Testing.apply _),
          (
            (__ \ "a").write[Int] and
            (__ \ "b").writeNullable[String]
          )(unlift(Testing.unapply))
        )
      }
    }

    check {
      (a: Int, b: Option[String]) =>
        val value = Testing(a, b)
        val jsonValue = Json.obj("a" -> a) ++ b
          .map(bValue => Json.obj("b" -> bValue))
          .getOrElse(Json.obj())

        // When
        val macroRead = Testing.macroFormat.reads(jsonValue)
        val handRead = Testing.handFormat.reads(jsonValue)

        // Then
        macroRead == handRead
    }
  }

  it should "support type aliases" in {
    // Given
    @JsonFormat
    case class Testing(id: Testing.Id, b: String)
    object Testing {
      type Id = Int
      val macroFormat: OFormat[Testing] = defaultFormat
        .idRead(Reads.min(1))
        .build

      val handFormat: OFormat[Testing] = {
        import play.api.libs.functional.syntax._
        import play.api.libs.json._
          (
            (__ \ "id").format[Id](Reads.min(1)) and
            (__ \ "b").format[String]
          )(Testing.apply, unlift(Testing.unapply))
      }
    }

    check {
      (a: Testing.Id, b: String) =>
        val value = Testing(a, b)
        val jsonValue = Json.obj("a" -> a, "b" -> b)

        // When
        val macroRead = Testing.macroFormat.reads(jsonValue)
        val handRead = Testing.handFormat.reads(jsonValue)

        // Then
        macroRead == handRead
    }
  }

}
