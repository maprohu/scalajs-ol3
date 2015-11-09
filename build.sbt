val commonSettings = Seq(
  scalaVersion := "2.11.7",
  organization := "com.github.maprohu"
)

lazy val facade = project
  .settings(commonSettings)
  .enablePlugins(JsdocPlugin, ScalaJSPlugin)
  .settings(
    name := "scalajs-ol3",
    jsdocDocletsFile := (sourceDirectory in Compile).value / "jsdoc" / "ol3-3.10.1-jsdoc.json",
    jsdocStripPrefix := "/home/marci/git/ol3/src/",
    sourceGenerators in Compile += jsdocGenerate.taskValue,
    jsDependencies ++= Seq(
      "org.webjars" % "openlayers" % "3.10.1" / "webjars/openlayers/3.10.1/ol-debug.js" minified "webjars/openlayers/3.10.1/ol.js"
    )

  )

lazy val testapp = project
  .settings(commonSettings)
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(facade)
