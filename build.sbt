lazy val commonSettings = Seq(
  organization := "net.fehmicansaglam",
  version := "0.3-SNAPSHOT",
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
) ++ publishSettings

shellPrompt in ThisBuild := Common.prompt

lazy val root = project.in(file("."))
  .aggregate(bson, tepkin, tepkinJava, pide, examples)
  .settings(commonSettings: _*)
  .settings(publishArtifact := false)

lazy val bson = project.in(file("bson"))
  .settings(commonSettings: _*)

lazy val tepkin = project.in(file("tepkin"))
  .settings(commonSettings: _*)
  .dependsOn(bson)

lazy val tepkinJava = project.in(file("tepkin-java"))
  .settings(commonSettings: _*)
  .dependsOn(tepkin)

lazy val pide = project.in(file("pide"))
  .settings(commonSettings: _*)
  .dependsOn(tepkin)

lazy val examples = project.in(file("examples"))
  .dependsOn(pide)
  .settings(commonSettings: _*)
  .settings(publishArtifact := false)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishArtifact in Test := false,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  },
  pomExtra := (
    <url>http://github.com/fehmicansaglam/tepkin</url>
      <licenses>
        <license>
          <name>Apache 2</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:fehmicansaglam/tepkin.git</url>
        <connection>scm:git@github.com:fehmicansaglam/tepkin.git</connection>
      </scm>
      <developers>
        <developer>
          <id>fehmicansaglam</id>
          <name>Fehmi Can Saglam</name>
          <url>http://github.com/fehmicansaglam</url>
        </developer>
      </developers>
    )
)
