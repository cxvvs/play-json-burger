package org.cxvvs.playjson

import play.api.libs.json._
import shapeless.{::, Generic, HList, HNil}
import shapeless.ops.hlist.Reverse

object ReadsMeta {
  def apply[T]: ReadsMetaBuilder[T] = new ReadsMetaBuilder[T]()
}

final class ReadsMetaBuilder[T] {
  def from[HLIST <: HList, REVERSE <: HList](
    builder: ReadsBuilder[HLIST]
  )(implicit _reverse: Reverse.Aux[HLIST, REVERSE],
    generic: Generic.Aux[T, REVERSE]): Reads[T] = new Reads[T] {
    def reads(json: JsValue): JsResult[T] =
      ReadsMetaBuilder.reads(json, builder.readList, generic)
  }
}

private[playjson] object ReadsMetaBuilder {
  def reads[HLIST <: HList, T](
    json: JsValue,
    readList: List[Reads[_]],
    generic: Generic.Aux[T, HLIST]
  ): JsResult[T] = {
    val validated: List[JsResult[_]] = readList.map(_.reads(json))
    val errors: List[JsError] = validated.collect {
      case err: JsError => err
    }

    if (errors.nonEmpty) {
      errors.foldLeft(JsError(Nil)) { case (acc, err) => acc ++ err }
    } else {
      val successHlist: HLIST = validated
        .collect { case JsSuccess(value, _) => value }
        .reverse
        .foldLeft[HList](HNil) { case (hlist, value) => value :: hlist }
        .asInstanceOf[HLIST]

      JsSuccess(generic.from(successHlist))
    }
  }
}

private[playjson] class ReadsBuilder[HLIST <: HList](
  val readList: List[Reads[_]]
) {
  def and[B](read: Reads[B]): ReadsBuilder[B :: HLIST] =
    new ReadsBuilder[B :: HLIST](readList :+ read)

  def ~[B](read: Reads[B]): ReadsBuilder[B :: HLIST] =
    and(read)
}
