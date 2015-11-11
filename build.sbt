val commonSettings = Seq(
  scalaVersion := "2.11.7",
  organization := "com.github.maprohu"
)

lazy val facade = project
  .settings(commonSettings)
  .enablePlugins(JsdocPlugin, ScalaJSPlugin)
  .dependsOn(generatorLib)
  .settings(
    name := "scalajs-ol3",
    jsdocDocletsFile := (sourceDirectory in Compile).value / "jsdoc" / "ol3-3.10.1-jsdoc.json",
    jsdocGlobalScope := Seq("ol3"),
    jsdocUtilScope := "pkg",
    sourceGenerators in Compile += jsdocGenerate.taskValue,
    jsDependencies ++= Seq(
      "org.webjars" % "openlayers" % "3.10.1" / "webjars/openlayers/3.10.1/ol-debug.js" minified "webjars/openlayers/3.10.1/ol.js"
    ),
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.0"
    )

  )

lazy val testapp = project
  .settings(commonSettings)
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(facade)
  .settings(
    persistLauncher in Compile := true,
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.0"
    )

  )

lazy val generatorLib = ProjectRef(file("generator/lib"), "lib")
