package io.github.edadma.mustache

import io.github.edadma.json.DefaultJSONReader

object Main extends App {

  val t =
    """
      |{{#data}}
      |  {{name}} -> {{value}}
      |{{/data}}
      |""".trim.stripMargin
  val json =
    """
      |{
      |  "data": [
      |    {"name": "a", "value": 1},
      |    {"name": "b", "value": 2}
      |  ]
      |}""".stripMargin
  val data = DefaultJSONReader.fromString(json)

  println(processMustache(data, t))

}
