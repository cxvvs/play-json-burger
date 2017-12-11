package org.cxvvs.playjson.macros

import scala.meta.Type

private[macros] case class TypeInfo(typeName: Type.Name, isOption: Boolean)
