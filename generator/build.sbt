
val commonSettings = Seq(
  organization := "com.github.maprohu",
  version := "0.1.0"
)

lazy val generator = (project in file("."))
  .settings(commonSettings)
  .settings(
    scalaVersion := "2.10.6",
    sbtPlugin := true,
    name := "generator-plugin",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "upickle" % "0.3.6",
      "org.scalamacros" %% s"quasiquotes" % "2.0.0" % "provided"
    )

  )


