import Dependencies._

scalaVersion in ThisBuild := "2.12.2"

scalacOptions in Global := Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-encoding",
  "utf-8", // Specify character encoding used by source files.
  "-explaintypes", // Explain type errors in more detail.
  "-feature", // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials", // Existential types (besides wildcard types) can be written and inferred
  "-language:higherKinds", // Allow higher-kinded types
  "-language:implicitConversions", // Allow definition of implicit functions called views
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
  "-Xlint:unsound-match", // Pattern match may not be typesafe.
  "-Ypartial-unification", // Enable partial unification in type constructor inference
  "-Ywarn-dead-code", // Warn when dead code is identified.
  "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen", // Warn when numerics are widened.
  "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals", // Warn if a local definition is unused.
  "-Ywarn-unused:params", // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars", // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates", // Warn if a private member is unused.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

scalafmtVersion := "1.2.0"
scalafmtOnCompile in ThisBuild := true

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      organization := "org.cxvvs",
      scalaVersion := "2.12.1",
      version := "0.1.0-SNAPSHOT"
    )),
  name := "playjson-meta",
  libraryDependencies ++= Seq(
    playJson,
    scalaMeta,
    shapeless,
    scalaTest % Test
  )
)
