package io.github.edadma.mustache

import io.github.edadma.json
import io.github.edadma.json.DefaultJSONReader
import xyz.hyperreal.pretty._

object Main extends App {

  val t =
    """
    |{{#musketeers}}
    |* {{.}}
    |{{/musketeers}}
    |""".trim.stripMargin
  val hash =
    """
      |{
      |  "musketeers": ["Athos", "Aramis", "Porthos", "D'Artagnan"]
      |}
     """.stripMargin
  val data = DefaultJSONReader.fromString(hash)
  val res =
    processMustache(
      data,
      t,
      predefs = List("user" -> MustacheParser.parse("<strong>{{name}}</strong>", defaultOptions)),
//      List(
      /*"missingIsException" -> true*/ /*, "trim" -> false*/ /*, "removeNonSectionBlanks" -> false*/ /*, "removeSectionBlanks" -> true*/
    )

  println(res)

}
