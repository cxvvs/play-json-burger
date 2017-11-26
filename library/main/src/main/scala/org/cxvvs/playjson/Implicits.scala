package org.cxvvs.playjson

import scala.language.implicitConversions
import play.api.libs.json.{OFormat, Reads}
import shapeless.{::, HNil}

object Implicits {
  implicit def formatToBuilder[T](format: OFormat[T]): Builder[T :: HNil] =
    new Builder[T :: HNil](List(format))

  implicit def readsToBuilder[T](read: Reads[T]): ReadsBuilder[T :: HNil] =
    new ReadsBuilder[T :: HNil](List(read))
}
