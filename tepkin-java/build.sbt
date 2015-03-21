name := "tepkin-java"

version := "0.2-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.11" % Test,
  "org.scalatest" %% "scalatest" % Dependencies.scalatestV % Test
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
