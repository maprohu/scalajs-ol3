logLevel := Level.Warn


lazy val root = (project in file("."))
  .dependsOn(generatorPlugin)
  .settings(

  )

lazy val generatorPlugin = ProjectRef(file("../generator"), "generator")

addSbtPlugin("com.github.maprohu" % "generator-plugin" % "0.1.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.5")