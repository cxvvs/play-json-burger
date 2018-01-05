import Dependencies._

lazy val library = (project in file("library"))
  .settings(
    name := "playjson-burger",
    organization  := "org.cxvvs",
    scalaVersion := "2.12.3",
    crossScalaVersions := Seq("2.11.12", "2.12.3"),
    libraryDependencies ++= Seq(
      playJson,
      scalaMeta,
      shapeless,
      scalaTest % Test,
      scalaCheck % Test
    ),
    publishTo := {
      val nexus = "https://oss.sonatype.org"
      if (isSnapshot.value) Some("snapshots" at s"$nexus/content/repositories/snapshots")
      else Some("releases" at s"$nexus/service/local/staging/deploy/maven2")
    },
    licenses := Seq("MIT License" -> url("http://opensource.org/licenses/mit-license.php")),
    scmInfo := Some(
      ScmInfo(
        url(s"https://github.com/cxvvs/${name.value}"),
        s"scm:git:git@github.com:cxvvs/${name.value}.git"
      )
    ),
    pomExtra :=
      <developers>
        <developer>
          <name>Christophe Venevongsos</name>
        </developer>
      </developers>,
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "utf-8",
      "-explaintypes",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-unchecked",
      "-Xlint:adapted-args",
      "-Xlint:inaccessible",
      "-Xlint:unsound-match",
      "-Ypartial-unification",
      "-Ywarn-dead-code",
      "-Ywarn-nullary-unit",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused:imports",
      "-Ywarn-value-discard"
    )
  )
  .settings(macroSettings : _*)

lazy val macroSettings = Seq(
  scalacOptions += "-Xplugin-require:macroparadise",
  addCompilerPlugin(
    "org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full)
)
