
val commonSettings = Seq(
  organization := "com.github.maprohu",
  version := "0.1.0"
)


lazy val lib = (project in file("."))
  .settings(commonSettings)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    scalaVersion := "2.11.7"
  )

