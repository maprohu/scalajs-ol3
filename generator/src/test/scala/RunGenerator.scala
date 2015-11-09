import java.io.{PrintWriter, File}

import jsdocgen._

import scala.io.Source

import jsdocgen.pickle._

/**
 * Created by marci on 08-11-2015.
 */
object RunGenerator extends App {

  val doclets = {
    val json = Source.fromURI(getClass.getResource("/ol3-3.10.1-jsdoc.json").toURI).mkString
    read[Seq[Doclet]](json)
  }

//  println(doclets.size)
//
//  val objects = doclets.collect {
//    case f @ Function(name, memberof, "static", _, _) => f
//  }.groupBy(_.memberof).map { case (memberof, fs) =>
//    s"""object $memberof {
//       |${fs.map(f => s"  def ${f.name}()").mkString("\n")}
//       |}
//     """.stripMargin
//  }
//
//  println(objects.mkString("\n"))


  def generate(
    target: File,
    doclets: Seq[Doclet],
    stripPrefix : String
  ) : Seq[File] = {

    def directory(path: String) : Option[generated.Directory] = {
      val elems = path.substring(stripPrefix.length).split('/')
      elems.foldLeft(Option.empty[generated.Directory])((p, n) => Some(generated.Directory(n, p)))
    }

    def sourceFile(path: String, filename: String) : generated.SourceFile = {
      val scalaFileName = filename.substring(0, filename.lastIndexOf('.'))
      generated.SourceFile(scalaFileName, directory(path))
    }

    def namespace(elems: Seq[String]) : Option[generated.Package] = {
      elems.foldLeft(Option.empty[generated.Package])((p, n) => Some(generated.Package(n , p)))
    }

    def generatedObject(path: String, filename: String, memberof: String) : generated.Object = {
      val elems = memberof.split('.')

      generated.Object(
        elems.last,
        namespace(elems.init),
        sourceFile(path, filename)
      )
    }


    val members = doclets.collect {
      case f : Function if f.scope == "static" => generated.Method(
        f.name,
        generatedObject(
          f.meta.path,
          f.meta.filename,
          f.memberof
        )
      )
    }

    def dir(directory: Option[generated.Directory]) : File = {
      directory.map(d => new File(dir(d.parent), d.name)).getOrElse(target)
    }

    def pkg(ns: generated.Package) : String = {
      ns.parent.map(p => pkg(p) + ".").getOrElse("") + ns.name
    }
    def pkgOpt(ns: Option[generated.Package]) : String = {
      ns.map(n => pkgOpt(n.parent) + "." + n.name).getOrElse("_root_")
    }

    val fs = members
      .groupBy(_.container.sourceFile)
      .map { case (sourceFile, member) =>
        val d = dir(sourceFile.directory)
        d.mkdirs()
        val f: File = new File(d, sourceFile.baseName + ".scala")
        val out = new PrintWriter(f)

        member
          .groupBy(_.container.namespace)
          .foreach { case (ns, nsmembers) =>
            out.write(
              s"""
                 |package ${pkg(ns.getOrElse(generated.Package("_error_", None)))} {
                 |}
               """.stripMargin
            )
          }

        out.close()

        f
      }

    fs.toList
  }

  generate(
    new File("target/generated"),
    doclets,
    "/home/marci/git/ol3/src/"
  )

}
