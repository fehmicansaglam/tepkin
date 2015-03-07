lazy val commonSettings = Seq(
  organization := "net.fehmicansaglam",
  scalaVersion := "2.11.6",
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
  ),
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oD")
)

shellPrompt in ThisBuild := Common.prompt

lazy val root = project.in(file("."))
  .aggregate(bson, tepkin, tepkinJava)

lazy val bson = project.in(file("bson"))
  .settings(commonSettings: _*)

lazy val tepkin = project.in(file("tepkin"))
  .settings(commonSettings: _*)
  .dependsOn(bson)

lazy val tepkinJava = project.in(file("tepkin-java"))
  .settings(commonSettings: _*)
  .dependsOn(tepkin)
