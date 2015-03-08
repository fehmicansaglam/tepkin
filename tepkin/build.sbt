name := "tepkin"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
  "com.typesafe.akka" %% "akka-actor" % Dependencies.akkaV,
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-M4",
  "org.scalatest" %% "scalatest" % Dependencies.scalatestV % Test
)
