package org.cxvvs.playjson

import org.cxvvs.playjson.builders.{FormatBuilder, ReadsBuilder, WritesBuilder}

import scala.language.implicitConversions
import play.api.libs.json.{OFormat, OWrites, Reads}
import shapeless.{::, HNil}

object Implicits {
  implicit def formatToBuilder[T](format: OFormat[T]): FormatBuilder[T :: HNil] =
    new FormatBuilder[T :: HNil](List(format))

  implicit def readsToBuilder[T](read: Reads[T]): ReadsBuilder[T :: HNil] =
    new ReadsBuilder[T :: HNil](List(read))

  implicit def writesToBuilder[T](writes: OWrites[T]): WritesBuilder[T :: HNil] =
    new WritesBuilder[T :: HNil](List(writes))
}
