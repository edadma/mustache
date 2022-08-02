package io.github.edadma.mustache

import io.github.edadma.json
import io.github.edadma.json.DefaultJSONReader

object Main extends App {

  val hash =
    """
      |{
      |  "repo": [
      |    { "name": "resque" },
      |    { "name": "hub" },
      |    { "name": "rip" }
      |  ],
      |  "asdf": 123
      |}
     """.stripMargin
  val t =
    """
    |{{#repo}}
    |  {{name}}, {{_}}
    |{{/repo}}
    |""".trim.stripMargin
  val data = DefaultJSONReader.fromString(hash)
  val res =
    processMustache(
      data,
      t,
//      List(
      /*"missingIsException" -> true*/ /*, "trim" -> false*/ /*, "removeNonSectionBlanks" -> false*/ /*, "removeSectionBlanks" -> true*/
    )

  println(res)

}
