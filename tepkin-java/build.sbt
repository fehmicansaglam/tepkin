name := "tepkin-java"

version := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.11" % Test,
  "org.scalatest" % "scalatest_2.11" % Dependencies.scalatestV % Test
)

testOptions += Tests.Argument(TestFrameworks.JUnit, "-v")
