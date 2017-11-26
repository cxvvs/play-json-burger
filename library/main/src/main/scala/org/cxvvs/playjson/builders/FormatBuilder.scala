package org.cxvvs.playjson.builders

import play.api.libs.json.OFormat
import shapeless.{::, HList}

private[playjson] class FormatBuilder[HLIST <: HList](
  val formatList: List[OFormat[_]]
) {
  def and[B](format: OFormat[B]): FormatBuilder[B :: HLIST] =
    new FormatBuilder[B :: HLIST](formatList :+ format)

  def ~[B](format: OFormat[B]): FormatBuilder[B :: HLIST] =
    and(format)
}
