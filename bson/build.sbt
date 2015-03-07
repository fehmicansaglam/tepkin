name := "bson"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % Dependencies.akkaV,
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  "org.scalatest" % "scalatest_2.11" % Dependencies.scalatestV % Test
)
