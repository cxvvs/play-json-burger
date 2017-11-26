package org.cxvvs.playjson

import play.api.libs.json._
import shapeless.{::, Generic, HList}
import shapeless.ops.hlist.Reverse

object WritesMeta {
  def apply[T]: WritesMetaBuilder[T] = new WritesMetaBuilder[T]()
}

final class WritesMetaBuilder[T] {
  def from[HLIST <: HList, REVERSE <: HList](
    builder: WritesBuilder[HLIST]
  )(implicit _reverse: Reverse.Aux[HLIST, REVERSE],
    generic: Generic.Aux[T, REVERSE]): OWrites[T] = new OWrites[T] {
    def writes(o: T): JsObject = {
      generic
        .to(o)
        .runtimeList
        .zip(builder.writesList)
        .foldLeft(Json.obj()) {
          case (acc, (value, writes)) =>
            acc ++ writes
              .asInstanceOf[OWrites[Any]]
              .writes(value.asInstanceOf[Any])
        }
    }
  }
}

private[playjson] object WritesMetaBuilder {
  def writes[HLIST <: HList, T](
    t: T,
    writesList: List[Writes[_]],
    generic: Generic.Aux[T, HLIST]
  ): JsObject = {
    generic
      .to(t)
      .runtimeList
      .zip(writesList)
      .foldLeft(Json.obj()) {
        case (acc, (value, writes)) =>
          acc ++ writes
            .asInstanceOf[OWrites[Any]]
            .writes(value.asInstanceOf[Any])
      }
  }
}

private[playjson] class WritesBuilder[HLIST <: HList](
  val writesList: List[OWrites[_]]
) {
  def and[B](writes: OWrites[B]): WritesBuilder[B :: HLIST] =
    new WritesBuilder[B :: HLIST](writesList :+ writes)

  def ~[B](writes: OWrites[B]): WritesBuilder[B :: HLIST] =
    and(writes)
}
