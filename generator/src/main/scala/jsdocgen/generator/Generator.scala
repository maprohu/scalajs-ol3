package jsdocgen.generator

import java.io.{File, PrintWriter}

import jsdocgen._
import jsdocgen.domain.pickle._
import jsdocgen.domain.{PackageMember, Doclet, Function, pickle}

import scala.io.Source

/**
 * Created by marci on 08-11-2015.
 */
object Generator {
  val reserved = Set(
    "clone",
    "toString"
  )

  val keyword = Set(
    "type"
  )
  def isReserved(name: String) = reserved.contains(name)

  def id(from: String) =
    if (isReserved(from)) from + "_"
    else if (keyword.contains(from)) s"`$from`"
    else from

  def generate(
    target: File,
    docletsFile: File,
    rootPackage : String,
    utilPackage : String
  ) : Seq[File] = {
    println("reading json: " + docletsFile)
    val doclets = {
      val json = Source.fromFile(docletsFile, "UTF-8").mkString
      read[Seq[Doclet]](json)
    }

    generate(
      target,
      doclets,
      rootPackage,
      utilPackage
    )
  }

  def generate(
    targetFile: File,
    doclets: Seq[Doclet],
    rootPackage : String,
    utilPackage : String
  ) : Seq[File] = {

    val namespaces = doclets
      .collect({
        case d : domain.Namespace => d
      })

    val functions = doclets
      .collect({
        case d : domain.Function => d
      })

    val classes = doclets
      .collect({
        case d : domain.Class => d
      })

    val typedefs = doclets
      .collect({
        case d : domain.Typedef => d
      })

    val members = doclets
      .collect({
        case d : domain.Member => d
      })

    val namespaceByParent = namespaces.groupBy(_.memberof).withDefaultValue(Seq())
    val functionByParent = functions.groupBy(_.memberof).withDefaultValue(Seq())
    val classByParent = classes.groupBy(_.memberof).withDefaultValue(Seq())
    val typedefByParent = typedefs.groupBy(_.memberof).withDefaultValue(Seq())
    val memberByParent = members.groupBy(_.memberof).withDefaultValue(Seq())

    targetFile.getParentFile.mkdirs()
    val out = new PrintWriter(targetFile)

    val builtins = Map(
      "string" -> "String"
    )
    def resolve(name: String) : String =
      builtins
        .get(name)
        .getOrElse("scala.scalajs.js.Any")

    def resolveUnion(t: domain.Type) : Set[String] =
      t.names.map(n => resolve(n)).toSet

    def resolveType(name: domain.Type) : String =
      resolve(name.names(0))

    def resolveMember(name: domain.Member) : String = {
      val types = resolveUnion(name.`type`)
      if (types.size > 1)
        s"${utilPackage}.implicits.`${name.longname}`"
      else
        types.toSeq.headOption
          .getOrElse("scala.scalajs.js.Any")
    }

    def resolveReturn(ret: Option[domain.Return]) : String =
      ret
        .map(name => resolveType(name.`type`))
        .getOrElse("Unit")



    def indent(str: String, level: Int) : String = {
      str.split('\n').map(("  " * level) + _).mkString("\n")
    }

    def writeout(str: String, level: Int = 0) = out.println(indent(str, level))

    def writeStatics(nsName: String, level: Int) : Unit = {

      def write(str: String) = writeout(str, level)

      write("")

      for {
        fn <- functionByParent(nsName)
      } {
        if (isReserved(fn.name))
          write(s"""@scala.scalajs.js.annotation.JSName("${fn.name}")""")
        write(s"def ${id(fn.name)}(")

        write(
          (for { p <- fn.params } yield {
            s"  ${id(p.name)} : scala.scalajs.js.UndefOr[${resolveType(p.`type`)}] = scala.scalajs.js.undefined"
          }).mkString(",\n")
        )
        write(s") : ${resolveReturn(fn.returns)} = scala.scalajs.js.native")
        write("")
      }

    }
    def writeNamespace(nsName: String, level: Int) : Unit = {

      def write(str: String) = writeout(str, level)

      write("")

      for {
        cl <- classByParent(nsName)
      } {
        write("@scala.scalajs.js.native")
        write(s"class ${cl.name} extends scala.scalajs.js.Object {")

        if (!cl.params.isEmpty) {
          write("  def this(")
          write(
            cl.params.map({ param =>
              s"    ${id(param.name)} : scala.scalajs.js.Any"
            }).mkString(",\n")
          )
          write("  ) = this()")
        }

        write(s"}")
        write("")
      }

      for {
        td <- typedefByParent(nsName)
      } {
        write("@scala.scalajs.js.native")
        write(s"trait ${td.name} extends scala.scalajs.js.Object {")

        val mems = memberByParent(td.longname)

        for {
          m <- mems
        } {
          if (isReserved(m.name))
            write(s"""@scala.scalajs.js.annotation.JSName("${m.name}")""")

          write(s"  var ${id(m.name)} : ${resolveMember(m)} = scala.scalajs.js.native")
        }

        write(s"}")

        write("")
      }

      for {
        ns <- namespaceByParent(nsName)
      } {
        write("@scala.scalajs.js.native")
        write(s"object ${ns.name} extends scala.scalajs.js.Object {")

        writeStatics(ns.longname, level+1)
        writeNamespace(ns.longname, level+1)

        write(s"}")
        write("")
      }


    }

    def writeUtilNamespace(nsName: String, level: Int) : Unit = {

      def write(str: String) = writeout(str, level)

      write("")

      for {
        td <- typedefByParent(nsName)
      } {

        val mems = memberByParent(td.longname)

        write(s"object ${td.name} {")
        write(s"  def apply(")
        write(
          (for { m <- mems } yield {
            s"    ${id(m.name)} : scala.scalajs.js.UndefOr[${resolveMember(m)}] = scala.scalajs.js.undefined"
          }).mkString(",\n")
        )
        write(s"  ) = scala.scalajs.js.Dynamic.literal(")
        write(
          (for { m <- mems } yield {
            s"""    "${m.name}" -> ${id(m.name)}"""
          }).mkString(",\n")
        )
        write(s"  ).asInstanceOf[${rootPackage}.${td.longname}]")

        write(s"}")


        write("")
      }

      for {
        ns <- namespaceByParent(nsName)
      } {
        write(s"package ${ns.name} {")

        writeUtilNamespace(ns.longname, level+1)

        write(s"}")
        write("")
      }


    }

    writeout(s"package ${rootPackage} {")

    writeout(s"  @scala.scalajs.js.native")
    writeout(s"  object globals extends scala.scalajs.js.GlobalScope {")
    writeStatics("", 2)
    writeout(s"  }")

    writeNamespace("", 1)
    writeout(s"}")

    writeout(s"package ${utilPackage} {")
    writeUtilNamespace("", 1)

    writeout(s"  object implicits {")

    for {
      td <- typedefs
      m <- memberByParent(td.longname)
      union = resolveUnion(m.`type`)
      if union.size > 1
    } {
      writeout(s"    @scala.scalajs.js.native")
      writeout(s"    trait `${m.longname}` extends scala.scalajs.js.Any")

      for {
        t <- union
      } {
        writeout(s"    implicit def `$t -> ${m.longname}`(v: $t) = v.asInstanceOf[`${m.longname}`]")
        writeout(s"    implicit def `$t -> UndefOr ${m.longname}`(v: $t) = v.asInstanceOf[scala.scalajs.js.UndefOr[`${m.longname}`]]")
      }


    }

    writeout(s"  }")

    writeout(s"}")

    out.close()
    Seq(targetFile)

  }


}

