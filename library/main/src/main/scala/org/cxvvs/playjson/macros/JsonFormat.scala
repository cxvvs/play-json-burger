package org.cxvvs.playjson.macros

import scala.annotation.compileTimeOnly
import scala.collection.immutable.Seq
import scala.meta._

@compileTimeOnly("Enable macro-paradise plugin to expand macro annotations")
class JsonFormat extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {

    def paramType(argType: Type.Arg): Type = {
      Type.Name(argType.syntax)
    }

    def createFormat(name: Type.Name, ctor: Ctor.Primary): Defn.Def = {
      val paramsFormat: Seq[Term.ApplyType] = ctor.paramss.head
        .map { parameter =>
          val fieldName: String = parameter.name.value
          val fieldType: Type = paramType(parameter.decltpe.get)
          // TODO : Handle nullable if Opt
          q"""(play.api.libs.json.JsPath \ $fieldName).format[$fieldType]"""
        }

      val concatParams = paramsFormat
        .tail
        .foldLeft[Term](paramsFormat.head) {
          case (acc, current) => q"$acc and $current"
        }

      q"""
        def macroFormat: play.api.libs.json.OFormat[$name] = {
          import org.cxvvs.playjson.Implicits._
          org.cxvvs.playjson.FormatMeta[$name].from($concatParams)
        }
      """
    }

    defn match {
      case Term.Block(Seq(
        cls @ Defn.Class(_, name, _, ctor, template),
        companion: Defn.Object
      )) =>
        val stats: Seq[Stat] = createFormat(name, ctor) +: companion.templ.stats.getOrElse(Nil)
        val template: Template = companion.templ.copy(stats = Some(stats))
        val augmentedCompanion = companion.copy(templ = template)
        Term.Block(Seq(
          cls, augmentedCompanion
        ))

      case _ =>
        abort("@JsonFormat must annotate a case class with a companion object")
    }
  }
}
