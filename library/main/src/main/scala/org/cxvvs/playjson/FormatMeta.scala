package org.cxvvs.playjson

import org.cxvvs.playjson.builders.FormatBuilder
import play.api.libs.json.{JsObject, JsResult, JsValue, OFormat}
import shapeless.{Generic, HList}
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
