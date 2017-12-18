package org.cxvvs.playjson.macros

import play.api.libs.json.JsPath

import scala.annotation.compileTimeOnly
import scala.collection.immutable.Seq
import scala.meta._

@compileTimeOnly("Enable macro-paradise plugin to expand macro annotations")
class JsonFormat extends scala.annotation.StaticAnnotation {
  inline def apply(defn: Any): Any = meta {

    def declarePreparedTrait(className: Type.Name, ctor: Ctor.Primary): Defn.Trait = {

      val readsModifiers: Seq[Decl.Def] = ctor.paramss.head
        .map { parameter =>
          val fieldName: String = parameter.name.value
          val TypeInfo(typeName, _) = JsonFormat.getType(parameter.decltpe.get)
          val methodName: Term.Name = Term.Name(s"${fieldName}Read")
          q"def $methodName(reads: play.api.libs.json.Reads[$typeName]): PreparedFormat"
        }

      q"""
        trait PreparedFormat {
          ..$readsModifiers;
          def build: play.api.libs.json.OFormat[$className]
        }
       """
    }

    // Call `.format` or `.formatNullable` on a JsPath
    def fieldFormat(jsPath: Term, fieldName: String, argType: Type.Arg): Term.Apply = {
      val TypeInfo(typeName, isOption) = JsonFormat.getType(argType)
      val readConstraint = Term.Name(s"read$fieldName")

      if(isOption)
        q"JsonFormat.withConstraintOpt($jsPath, $jsPath.formatNullable[$typeName], $readConstraint)"
      else
        q"$jsPath.format[$typeName]($readConstraint.getOrElse(implicitly[Reads[$typeName]]))"
    }

    def createFormat(className: Type.Name, ctor: Ctor.Primary): Defn.Def = {
      val paramsFormat: Seq[Term.Apply] = ctor.paramss.head
        .map { parameter =>
          val fieldName: String = parameter.name.value
          val jsPath: Term = q"""(play.api.libs.json.JsPath \ $fieldName)"""
          fieldFormat(jsPath, fieldName, parameter.decltpe.get)
        }

      val concatParams: Term = paramsFormat
        .tail
        .foldLeft[Term](paramsFormat.head) {
          case (acc, current) =>
            q"""$acc and $current"""
        }

      def readFields(getFieldName: (String) => Term.Name): Seq[Defn.Val] = ctor.paramss.head
        .map { parameter =>
          val fieldName: String = parameter.name.value
          val TypeInfo(typeName, _) = JsonFormat.getType(parameter.decltpe.get)
          val patVarTerm: Pat.Var.Term = Pat.Var.Term(Term.Name(s"read$fieldName"))
          q"val $patVarTerm: Option[play.api.libs.json.Reads[$typeName]] = ${getFieldName(fieldName)}"
        }

      // TODO : Remove copy paste (defined in trait)
      val readModifiers: Seq[Defn.Def] = ctor.paramss.head
        .map { parameter =>
          val fieldName: String = parameter.name.value
          val TypeInfo(typeName, _) = JsonFormat.getType(parameter.decltpe.get)
          val methodName: Term.Name = Term.Name(s"${fieldName}Read")
          val readFieldName: Term.Name = Term.Name(s"replace$fieldName")
          q"""
              def $methodName(reads: play.api.libs.json.Reads[$typeName]): PreparedFormat =
                this.copy($readFieldName = Some(reads))
            """
        }

      val copyFields: Seq[Term.Param] = ctor.paramss.head
        .map { ctorParameter =>
          // Parameter name : replaceXXX
          val parameterName: Term.Param.Name = Term.Name(s"replace${ctorParameter.name}")
          val TypeInfo(typeName, _) = JsonFormat.getType(ctorParameter.decltpe.get)
          val readType: Type.Name = Type.Name(s"Option[play.api.libs.json.Reads[$typeName]]")

          // Default value : readXXX
          val defaultValue: Term.Name = Term.Name(s"read${ctorParameter.name}")

          param"$parameterName: $readType = $defaultValue"
        }

      val copyParameterNames: Seq[Term.Name] = ctor.paramss.head
          .map { ctorParameter => Term.Name(s"replace${ctorParameter.name}") }

      val copyIncantation: Defn.Def =
        q"""
            def copy(..$copyFields): PreparedFormat = originalCopy(..$copyParameterNames)
         """

      val buildIncantation: Defn.Def =
        q"""
            def build: play.api.libs.json.OFormat[$className] = {
              import org.cxvvs.playjson.Implicits._
              org.cxvvs.playjson.FormatMeta[$className].from($concatParams)
            }
         """

      val originalCopyMethod: Defn.Def =
        q"""
            def originalCopy(..$copyFields): PreparedFormat = new PreparedFormat {
              ..${readFields(fieldName => Term.Name(s"replace$fieldName"))}
              ..$readModifiers
              $copyIncantation
              $buildIncantation
            }
         """

      q"""
        def preparedFormat: PreparedFormat =
          new PreparedFormat {

            ..${readFields(_ => q"None")};
            ..$readModifiers;

            $originalCopyMethod;
            $copyIncantation;

            $buildIncantation;
          }
      """
    }

    defn match {
      case Term.Block(Seq(
        cls @ Defn.Class(_, className, _, ctor, template),
        companion: Defn.Object
      )) =>
        val stats: Seq[Stat] =
          declarePreparedTrait(className, ctor) +:
          createFormat(className, ctor) +:
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

object JsonFormat {

  private[macros] def getType(argType: Type.Arg): TypeInfo = {
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

  import play.api.libs.json.{OFormat, Reads, JsValue, JsResult, JsObject}

  /**
    * The technique used for format doesn't work for `formatNullable`
    * because `.formatNullable[T](r: Reads[T])` does not exist
    *
    * This function manually handles that case
    */
  def withConstraintOpt[T : Reads](
    jsPath: JsPath,
    format: OFormat[Option[T]],
    optReads: Option[Reads[T]]
  ): OFormat[Option[T]] = {
    new OFormat[Option[T]] {
      def reads(json: JsValue): JsResult[Option[T]] = {
        val originalReads: Reads[Option[T]] = Reads(format.reads)
        optReads
          .map(reads => Reads.nullable(jsPath)(implicitly[Reads[T]]))
          .getOrElse(originalReads)
          .reads(json)
      }
      def writes(o: Option[T]): JsObject =
        format.writes(o)
    }
  }
}
