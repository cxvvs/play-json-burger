import sbt._

object Dependencies {
  private val shapelessVersion = "2.3.2"
  private val scalaTestVersion = "3.0.1"
  private val playJsonVersion = "2.6.3"
  private val scalaMetaVersion = "1.8.0"
  private val scalaCheckVersion = "1.13.4"

  val shapeless = "com.chuusai" %% "shapeless" % shapelessVersion
  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
  val scalaCheck =  "org.scalacheck" %% "scalacheck" % scalaCheckVersion
  val playJson = "com.typesafe.play" %% "play-json" % playJsonVersion
  val scalaMeta = "org.scalameta" %% "scalameta" % scalaMetaVersion
}
