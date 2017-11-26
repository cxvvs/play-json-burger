package org.cxvvs.playjson

import play.api.libs.json._
import shapeless.{::, Generic, HList}
import shapeless.ops.hlist.Reverse

object FormatMeta {
  def apply[T]: FormatMetaBuilder[T] = new FormatMetaBuilder[T]()
}

final class FormatMetaBuilder[T] {
  def from[HLIST <: HList, REVERSE <: HList](
      builder: Builder[HLIST]
  )(implicit _reverse: Reverse.Aux[HLIST, REVERSE],
    generic: Generic.Aux[T, REVERSE]): OFormat[T] = new OFormat[T] {
    def reads(json: JsValue): JsResult[T] =
      ReadsMetaBuilder.reads(json, builder.formatList, generic)

    def writes(o: T): JsObject = {
      generic
        .to(o)
        .runtimeList
        .zip(builder.formatList)
        .foldLeft(Json.obj()) {
          case (acc, (value, format)) =>
            acc ++ format
              .asInstanceOf[OFormat[Any]]
              .writes(value.asInstanceOf[Any])
        }
    }
  }
}

private[playjson] class Builder[HLIST <: HList](
    val formatList: List[OFormat[_]]
) {
  def and[B](format: OFormat[B]): Builder[B :: HLIST] =
    new Builder[B :: HLIST](formatList :+ format)

  def ~[B](format: OFormat[B]): Builder[B :: HLIST] =
    and(format)
}
