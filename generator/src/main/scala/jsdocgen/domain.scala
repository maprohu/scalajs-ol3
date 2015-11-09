package jsdocgen

import pickle.key

case class Meta(
  filename: String,
  path: String
)

sealed trait Doclet

@key("function") case class Function(
  name: String,
  memberof: String,
  scope: String,
  meta: Meta,
  params: Seq[Param] = Seq()
) extends Doclet

case class Param(
  `type`: Type,
  name: String

)

case class Type(
  names: Seq[String]

)
@key("member") case class Member(
  name: String
) extends Doclet

@key("namespace") case class Namespace(
  name: String
) extends Doclet

@key("class") case class Class(
  name: String,
  memberof: String,
  scope: String
) extends Doclet

@key("typedef") case class Typedef(
) extends Doclet

@key("event") case class Event(
) extends Doclet

@key("constant") case class Constant(
) extends Doclet

@key("interface") case class Interface(
) extends Doclet

@key("package") case class Package(
) extends Doclet

object pickle extends upickle.AttributeTagged {
  def tagName = "kind"
}

