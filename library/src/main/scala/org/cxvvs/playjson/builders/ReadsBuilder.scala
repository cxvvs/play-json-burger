package org.cxvvs.playjson.builders

import play.api.libs.json.{Reads}
import shapeless.{::, HList}

private[playjson] class ReadsBuilder[HLIST <: HList](
  val readList: List[Reads[_]]
) {
  def and[B](read: Reads[B]): ReadsBuilder[B :: HLIST] =
    new ReadsBuilder[B :: HLIST](readList :+ read)

  def ~[B](read: Reads[B]): ReadsBuilder[B :: HLIST] =
    and(read)
}
