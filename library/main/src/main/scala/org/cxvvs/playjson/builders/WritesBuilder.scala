package org.cxvvs.playjson.builders

import play.api.libs.json.{OWrites}
import shapeless.{::, HList}

private[playjson] class WritesBuilder[HLIST <: HList](
  val writesList: List[OWrites[_]]
) {
  def and[B](writes: OWrites[B]): WritesBuilder[B :: HLIST] =
    new WritesBuilder[B :: HLIST](writesList :+ writes)

  def ~[B](writes: OWrites[B]): WritesBuilder[B :: HLIST] =
    and(writes)
}
