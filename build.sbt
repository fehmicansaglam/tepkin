name := "tepkin"

organization := "net.fehmicansaglam"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.6"

scalacOptions := Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import" // 2.11 only
)

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.3",
  "com.typesafe.akka" %% "akka-actor" % "2.3.9",
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-M4",
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
