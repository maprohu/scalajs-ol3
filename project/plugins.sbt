logLevel := Level.Warn


//lazy val root = (project in file("."))
//  .dependsOn(jsdocgenPlugin)
//  .settings(
//
//  )
//
//lazy val jsdocgenPlugin = ProjectRef(uri("https://github.com/maprohu/scalajs-jsdocgen.git"), "plugin")

addSbtPlugin("com.github.maprohu" % "jsdocgen-plugin" % "0.1.0-SNAPSHOT")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.5")