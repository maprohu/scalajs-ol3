package jsdocgen.generator

import sbt._
import Keys._

object JsdocPlugin extends AutoPlugin {

  override val projectSettings: Seq[Setting[_]] = baseNonameSettings

  object autoImport {
    lazy val jsdocGenerate = taskKey[Seq[File]]("jsdoc-generate")

    lazy val jsdocTarget = taskKey[File]("jsdoc-target")

    lazy val jsdocDocletsFile = settingKey[File]("jsdoc-docletsfile")

    lazy val jsdocStripPrefix = settingKey[String]("jsdoc-stripprefix")
  }

  import autoImport._

  lazy val baseNonameSettings: Seq[sbt.Def.Setting[_]] = Seq(
    jsdocTarget := (sourceManaged in Compile).value / "jsdocgen",
    jsdocStripPrefix := "",
    jsdocGenerate := {
      Generator.generate(
        jsdocTarget.value,
        jsdocDocletsFile.value,
        jsdocStripPrefix.value
      )
    }
  )
}