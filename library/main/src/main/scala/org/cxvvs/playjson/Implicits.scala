package org.cxvvs.playjson

import play.api.libs.json.OFormat
import shapeless.{::, HNil}

object Implicits {
  implicit def formatToBuilder[T](format: OFormat[T]): Builder[T :: HNil] =
    new Builder[T :: HNil](List(format))
}
