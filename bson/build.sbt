name := "bson"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % Dependencies.akkaV,
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  "org.scalatest" %% "scalatest" % Dependencies.scalatestV % Test
)
