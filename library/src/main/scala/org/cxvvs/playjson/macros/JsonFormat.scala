package org.cxvvs.playjson.macros

import scala.annotation.compileTimeOnly
import scala.collection.immutable.Seq
import scala.meta._

@compileTimeOnly("Enable macro-paradise plugin to expand macro annotations")
class JsonFormat extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {

    def declarePreparedTrait(typeName: Type.Name): Defn.Trait = {
      q"""
        trait PreparedFormat {
          def build: play.api.libs.json.OFormat[$typeName]
        }
       """
    }

    // Call `.format` or `.formatNullable` on a JsPath
    def fieldFormat(jsPath: Term, argType: Type.Arg): Term.ApplyType = {
      val TypeInfo(typeName, isOption) = JsonFormat.getType(argType)

      if(isOption)
        q"$jsPath.formatNullable[$typeName]"
      else
        q"$jsPath.format[$typeName]"
    }

    def createFormat(name: Type.Name, ctor: Ctor.Primary): Defn.Def = {
      val paramsFormat: Seq[Term.ApplyType] = ctor.paramss.head
        .map { parameter =>
          val fieldName: String = parameter.name.value
          val jsPath: Term = q"""(play.api.libs.json.JsPath \ $fieldName)"""
          fieldFormat(jsPath, parameter.decltpe.get)
        }

      val concatParams: Term = paramsFormat
        .tail
        .foldLeft[Term](paramsFormat.head) {
          case (acc, current) => q"$acc and $current"
        }

      val readFields: Seq[Defn.Val] = ctor.paramss.head
        .map { case parameter =>
          val fieldName: String = parameter.name.value
          val TypeInfo(typeName, isOption) = JsonFormat.getType(parameter.decltpe.get)
          val readFieldName: Term.Name = Term.Name(s"read$fieldName")
          val patVarTerm: Pat.Var.Term = Pat.Var.Term(readFieldName)
          q"val $patVarTerm: Option[play.api.libs.json.Reads[$typeName]] = None"
        }

      val result = q"""
        def preparedFormat: PreparedFormat =
          new PreparedFormat {

            ..$readFields;

            def build: play.api.libs.json.OFormat[$name] = {
              import org.cxvvs.playjson.Implicits._
              org.cxvvs.playjson.FormatMeta[$name].from($concatParams)
            }
          }
      """

      println(result)

      result
    }

    defn match {
      case Term.Block(Seq(
        cls @ Defn.Class(_, name, _, ctor, template),
        companion: Defn.Object
      )) =>
        val stats: Seq[Stat] = declarePreparedTrait(name) +:
          createFormat(name, ctor) +:
          companion.templ.stats.getOrElse(Nil)
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

private[macros] object JsonFormat {

  def getType(argType: Type.Arg): TypeInfo = {
    val option = """Option\[(.+)]""".r

    argType.syntax match {
      case option(rawType) =>
        val tpe = Type.Name(rawType)
        TypeInfo(tpe, isOption = true)

      case rawType =>
        val tpe = Type.Name(rawType)
        TypeInfo(tpe, isOption = false)
    }
  }
}
