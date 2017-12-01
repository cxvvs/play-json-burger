import Dependencies._

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
  "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

name := "playjson-burger"

lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := "2.12.3"
)

lazy val macroSettings = Seq(
  scalacOptions += "-Xplugin-require:macroparadise",
  addCompilerPlugin(
    "org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full)
)

lazy val library = (project in file("library"))
  .settings(commonSettings: _*)
  .settings(macroSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      playJson,
      scalaMeta,
      shapeless,
      scalaTest % Test,
      scalaCheck % Test
    )
  )
