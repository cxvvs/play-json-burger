# Play-json Burger

Alternate combinators for `play-json` `Reads`, `OWrites` and `OFormat`.  

Allows the creation of `Formats` for case classes without the 22 fields limitation.  
Provides a macro (using the macro paradise plugin) to define `Formats` with the least amount of boilerplate.

## Motivation

Most people use the macros available with `play-json` to define JSON encoders / decoders :

```scala
case class User(id: UUID, age: Int, name: String, description: Option[String])

object User {
  implicit val format: OFormat[User] = Json.format[User]
}
```

It's great because it allows you to iterate pretty fast.  

Until you get to the part where you want to add constraints on the decoding part : 
the macro can not be used anymore and you need to do define your format manually :

```scala
  implicit val format: OFormat[User] = OFormat(
    (
      (JsPath \ "id").read[UUID] and
      (JsPath \ "age").read[Int](Reads.min(0)) and
      (JsPath \ "name").read[String](Reads.minLength[String](3)) and
      (JsPath \ "description").readNullable(Reads.minLength[String](1))
    )(User.apply _),
    (
      (JsPath \ "id").write[UUID] and
      (JsPath \ "age").write[Int] and
      (JsPath \ "name").write[String] and
      (JsPath \ "description").writeNullable[String]
    )(unlift(User.unapply))
  )
```

Manual definition for reads / writes / format becomes really tedious as more fields are added.  
What if we could say something like "I want the macro behavior with a few added constraints" ?  
With Burger you can do that in a totally safe way :

```scala
import org.cxvvs.playjson.macros.JsonFormat

@JsonFormat
case class User(id: UUID, age: Int, name: String, description: Option[String])

object User {
  implicit val format: OFormat[User] = preparedFormat
      .ageRead(Reads.min[Int](0))
      .nameRead(Reads.minLength[String](3))
      .descriptionRead(Reads.min[String](1))
      .build
}
```
