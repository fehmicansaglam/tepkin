name := "tepkin"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
  "com.typesafe.akka" %% "akka-actor" % Dependencies.akkaV,
  "com.typesafe.akka" %% "akka-stream" % Dependencies.akkaStreamV,
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-classic" % "1.0.13" % Test,
  "org.scalatest" %% "scalatest" % Dependencies.scalatestV % Test,
  "com.github.simplyscala" %% "scalatest-embedmongo" % "0.2.2" % Test intransitive(),
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "1.50.2" % Test
//  "com.github.simplyscala" %% "scalatest-embedmongo" % "0.2.2" % Test exclude("de.flapdoodle.embed","de.flapdoodle.embedmongo")
)
