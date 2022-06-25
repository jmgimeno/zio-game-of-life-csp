ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "3.1.2"

lazy val root = (project in file(".")).settings(
  name := "zio-game-of-life-csp",
  javacOptions ++= Seq("--release", "17"),
  libraryDependencies ++= Seq(
    "dev.zio" %% "zio" % "2.0.0",
    "dev.zio" %% "zio-test" % "2.0.0" % Test,
    "dev.zio" %% "zio-test-sbt" % "2.0.0" % Test
  ),
  testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
)
