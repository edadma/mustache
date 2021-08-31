package io.github.edadma.mustache

import io.github.edadma.json.DefaultJSONReader

object Main extends App {

//  val t =
//    """
//      |{{#data}}
//      |{{#section}}
//      |  {{name}} -> {{value}}
//      |{{/section}}
//      |{{/data}}
//      |""".trim.stripMargin
//  val json =
//    """
//      |{
//      |  "data": [
//      |    {
//      |      "section": [
//      |        {"name": "a", "value": 1},
//      |        {"name": "b", "value": 2}
//      |      ]
//      |    }
//      |  ]
//      |}""".stripMargin
//  val t =
//    """
//    |{{#data}}
//    |{{#section}}
//    |  {{name}} -> {{value}}
//    |{{/section}}
//    |
//    |{{/data}}
//    |""".trim.stripMargin
  val t = " asdf\n\nqwer \n"
  val json =
    """
    |{
    |  "data": [
    |    {
    |      "section": [
    |        {"name": "a", "value": 1},
    |        {"name": "b", "value": 2}
    |      ]
    |    },
    |    {
    |      "section": [
    |        {"name": "c", "value": 3},
    |        {"name": "d", "value": 4}
    |      ]
    |    }
    |  ]
    |}""".stripMargin
  val data = DefaultJSONReader.fromString(json)

//  println(processMustache(data, t, "removeNonSectionBlanks" -> false))

}

// todo: 2 unit tests are failing because newlines are lumped together with text and shouldn't be. the parser should break up lines
