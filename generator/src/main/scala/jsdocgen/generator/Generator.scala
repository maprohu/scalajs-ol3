package jsdocgen.generator

import java.io.{File, PrintWriter}

import jsdocgen._
import jsdocgen.domain.pickle._
import jsdocgen.domain.{PackageMember, Doclet, Function, pickle}
import jsdocgen.generated.Function

import scala.io.Source

/**
 * Created by marci on 08-11-2015.
 */
object Generator {
  val reserved = Set(
    "clone",
    "toString"
  )

  def isReserved(name: String) = reserved.contains(name)
  def id(from: String) = if (isReserved(from)) from + "_" else from

  def generate(
    target: File,
    docletsFile: File,
    stripPrefix : String
  ) : Seq[File] = {
    println("reading json: " + docletsFile)
    val doclets = {
      val json = Source.fromFile(docletsFile, "UTF-8").mkString
      read[Seq[Doclet]](json)
    }

    generate(
      target,
      doclets,
      stripPrefix
    )
  }

  def generate(
    target: File,
    doclets: Seq[Doclet],
    stripPrefix : String
  ) : Seq[File] = {


    val namespaceLongnames = doclets
      .collect({
        case d : domain.Namespace => d.longname
      })
      .toSet

    trait GeneratedFile {
      def getFile : File
      def path : Seq[String]
    }

    trait Namespaced {
      def name : String
      def path : Seq[String]
      def namespace : Seq[String]
    }

    class DefaultNamespaced(
      val name : String,
      val path: Seq[String],
      val namespace: Seq[String]
    ) extends Namespaced {
      def this(d: PackageMember) = this(
        d.name,
        namespace = d.memberof.split('.'),
        path = (d.meta.path.substring(stripPrefix.length).split('/') :+ stripExtension(d.meta.filename))
      )
    }


    trait PackageFunction extends Namespaced {

    }

    trait GeneratedClass extends Namespaced {
      def fullname : String

    }

    def stripExtension(filename: String) = {
      val idx = filename.lastIndexOf('.')
      if (idx == -1) filename else filename.substring(0, idx)
    }


    val packageFunctions : Seq[PackageFunction] = doclets
      .collect({
        case d : domain.Function if namespaceLongnames.contains(d.memberof) => new DefaultNamespaced(d) with PackageFunction
      })

    val generatedClasses : Seq[GeneratedClass] = doclets
      .collect({
        case d : domain.Class if namespaceLongnames.contains(d.memberof) => new DefaultNamespaced(d) with GeneratedClass {
          override def fullname: String = d.longname
        }

      })

    val generatedFiles = packageFunctions
      .map(_.path)
      .toSet

    val pfByPath = packageFunctions.groupBy(_.path).withDefaultValue(Seq())
    val classByPath = generatedClasses.groupBy(_.path).withDefaultValue(Seq())


    generatedFiles.toSeq.map { gf =>
      val f = new File(target, gf.mkString("/") + ".scala")
      f.getParentFile.mkdirs()
      val out = new PrintWriter(f)

      val pfByNamespace =  pfByPath(gf).groupBy(_.namespace).withDefaultValue(Seq())
      val classByNamespace =  classByPath(gf).groupBy(_.namespace).withDefaultValue(Seq())

      val namespaces = pfByNamespace.keySet ++ classByNamespace.keySet

      namespaces.foreach { ns =>

        val defs = pfByNamespace(ns)
          .map({pf =>
            val funDef = s"    def ${id(pf.name)} : Unit = scala.scalajs.js.native"
            (if (isReserved(pf.name))
              s"""    @scala.scalajs.js.annotation.JSName("${pf.name}")""" + "\n"
            else "") + funDef
          })

        val packageObject = {
          if (defs.isEmpty) "" else
            s"""
               |  @scala.scalajs.js.annotation.JSName("${ns.mkString(".")}")
               |  @scala.scalajs.js.native
               |  object ${ns.last} extends scala.scalajs.js.Object {
               |${defs.mkString("\n")}
               |  }
             """.stripMargin
        }


        val classes = classByNamespace(ns)
          .map({ cl =>
            s"""
               |  @scala.scalajs.js.annotation.JSName("${cl.fullname}")
               |  @scala.scalajs.js.native
               |  class ${cl.name} extends scala.scalajs.js.Object {
               |  }
             """.stripMargin
          })
          .mkString("\n")

        val packagePath = ns.init
        if (packagePath.isEmpty) {
          out.write(packageObject)
          out.write(classes)
        }
        else out.write(
          s"""
             |package ${packagePath.mkString(".")} {
             |$packageObject
             |$classes
             |}
           """.stripMargin
        )
      }

      out.close()
      f
    }


  }

//
//    def directory(path: String) : Option[jsdocgen.generated.Directory] = {
//      val elems = path.substring(stripPrefix.length).split('/')
//      elems.foldLeft(Option.empty[jsdocgen.generated.Directory])((p, n) => Some(generated.Directory(n, p)))
//    }
//
//    def sourceFile(path: String, filename: String) : jsdocgen.generated.SourceFile = {
//      val scalaFileName = filename.substring(0, filename.lastIndexOf('.'))
//      generated.SourceFile(scalaFileName, directory(path))
//    }
//
//    def namespace(elems: Seq[String]) : Option[jsdocgen.generated.Package] = {
//      elems.foldLeft(Option.empty[jsdocgen.generated.Package])((p, n) => Some(generated.Package(n , p)))
//    }
//
//    def generatedObject(path: String, filename: String, memberof: String) : jsdocgen.generated.Object = {
//      val elems = memberof.split('.')
//
//      generated.Object(
//        elems.last,
//        namespace(elems.init),
//        sourceFile(path, filename)
//      )
//    }
//
//    val longname = doclets
//      .collect({case ns:domain.Named => (ns.longname, ns)})
//      .toMap
//
//    val objectMembers = doclets
//      .collect({
//        case d : domain.ObjectMember => d
//      })
//
//    val namespaceMember
//
//
//    val members = doclets.collect {
//      case f : domain.Function if f.scope == "static" && namespaceMap.contains(f.memberof) => generated.Function(
//        f.name,
//        generatedObject(
//          f.meta.path,
//          f.meta.filename,
//          f.memberof
//        )
//      )
//    }
//
//    def dir(directory: Option[jsdocgen.generated.Directory]) : File = {
//      directory.map(d => new File(dir(d.parent), d.name)).getOrElse(target)
//    }
//
//    def pkg(ns: jsdocgen.generated.Package) : String = {
//      ns.parent.map(p => pkg(p) + ".").getOrElse("") + ns.name
//    }
//    def pkgOpt(ns: Option[jsdocgen.generated.Package]) : String = {
//      ns.map(n => pkgOpt(n.parent) + "." + n.name).getOrElse("_root_")
//    }
//
//    val fs = members
//      .groupBy(_.container.sourceFile)
//      .map { case (sourceFile, member) =>
//        val d = dir(sourceFile.directory)
//        d.mkdirs()
//        val f: File = new File(d, sourceFile.baseName + ".scala")
//        val out = new PrintWriter(f)
//
//        member
//          .groupBy(_.container.namespace)
//          .foreach { case (ns, nsmembers) =>
//
//
//            val objs = nsmembers
//              .groupBy(_.container)
//              .map { case (o, fs) =>
//                val functions = fs.collect {
//                  case m : generated.Function => s"    def ${id(m.name)} = scala.scalajs.js.native"
//                }
//
//                s"""
//                   |  @scala.scalajs.js.native
//                   |  object ${o.name} extends scala.scalajs.js.Object {
//                   |${functions.mkString("\n")}
//                   |  }
//               """.stripMargin
//              }
//
//            out.write(
//              s"""
//                 |package ${pkg(ns.getOrElse(generated.Package("_error_", None)))} {
//                 |${objs.mkString("\n")}
//                 |}
//               """.stripMargin
//            )
//          }
//
//        out.close()
//
//        f
//      }
//
//    fs.toList
//  }

}


