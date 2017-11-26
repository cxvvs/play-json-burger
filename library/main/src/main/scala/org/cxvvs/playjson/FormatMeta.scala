package org.cxvvs.playjson

import play.api.libs.json._
import shapeless.{::, Generic, HList}
import shapeless.ops.hlist.Reverse

object FormatMeta {
  def apply[T]: FormatMetaBuilder[T] = new FormatMetaBuilder[T]()
}

final class FormatMetaBuilder[T] {
  def from[HLIST <: HList, REVERSE <: HList](
      builder: FormatBuilder[HLIST]
  )(implicit _reverse: Reverse.Aux[HLIST, REVERSE],
    generic: Generic.Aux[T, REVERSE]): OFormat[T] = new OFormat[T] {
    def reads(json: JsValue): JsResult[T] =
      ReadsMetaBuilder.reads(json, builder.formatList, generic)

    def writes(o: T): JsObject =
      WritesMetaBuilder.writes(o, builder.formatList, generic)
  }
}

private[playjson] class FormatBuilder[HLIST <: HList](
    val formatList: List[OFormat[_]]
) {
  def and[B](format: OFormat[B]): FormatBuilder[B :: HLIST] =
    new FormatBuilder[B :: HLIST](formatList :+ format)

  def ~[B](format: OFormat[B]): FormatBuilder[B :: HLIST] =
    and(format)
}
