package generated


case class Directory(
  name: String,
  parent: Option[Directory]
)

case class SourceFile(
  baseName: String,
  directory: Option[Directory]
)

case class Package(
  name: String,
  parent: Option[Package]
)

trait TopLevel {
  val name: String
  val namespace: Option[Package]
  val sourceFile: SourceFile
}

case class Class(
  name: String,
  namespace: Option[Package],
  sourceFile: SourceFile
) extends TopLevel

case class Object(
  name: String,
  namespace: Option[Package],
  sourceFile: SourceFile
) extends TopLevel

case class Method(
  name: String,
  container: TopLevel
)

