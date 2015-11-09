name := "scalajs-ol3"

version := "1.0"


scalaVersion := "2.11.7"

val commonSettings = Seq(
  scalaVersion := "2.11.7"
)

lazy val generator = project
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "upickle" % "0.3.6"
    )


  )

