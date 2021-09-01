package io.github.edadma.mustache

import io.github.edadma.json
import io.github.edadma.json.DefaultJSONReader
import xyz.hyperreal.pretty._

object Main extends App {

//  val t =
//    """
//    |{{#data}}
//    |{{#section}}
//    |  {{name}} -> {{value}}
//    |{{/section}}
//    |{{/data}}
//    |""".trim.stripMargin
//  val json =
//    """
//    |{
//    |  "data": [
//    |    {
//    |      "section": [
//    |        {"name": "a", "value": 1},
//    |        {"name": "b", "value": 2}
//    |      ]
//    |    },
//    |    {
//    |      "section": [
//    |        {"name": "c", "value": 3},
//    |        {"name": "d", "value": 4}
//    |      ]
//    |    }
//    |  ]
//    |}""".stripMargin
  val t =
    """
    |{{#repo}}
    |  {{_._.asdf}}
    |{{/repo}}
    |""".trim.stripMargin
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
  val data = DefaultJSONReader.fromString(hash)

///  println(prettyPrint(MustacheParser.parse(t, defaultsOptions)))
  println(
    processMustache(
      data,
      t,
      "missingIsException" -> true /*, "trim" -> false*/ /*, "removeNonSectionBlanks" -> false*/ /*, "removeSectionBlanks" -> true*/ ))

}
