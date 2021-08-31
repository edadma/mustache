package io.github.edadma.mustache

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
    |{{#person?}}
    |  Hi {{name}}!
    |{{/person?}}
    |""".trim.stripMargin
  val json =
    """
      |{
      |  "person?": false
      |}""".stripMargin //"person?": { "name": "Jon" }
  val data = DefaultJSONReader.fromString(json)

//  println(prettyPrint(MustacheParser.parse(t, defaultsOptions)))
  println(
    processMustache(
      data,
      t /*, "trim" -> false*/ /*, "removeNonSectionBlanks" -> false*/ /*, "removeSectionBlanks" -> true*/ ))

}
