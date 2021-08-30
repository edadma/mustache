package io.github.edadma.mustache

object Main extends App {

  val t =
    """
      |{{#data}}
      |{{#section}}
      |  {{name}} -> {{value}}
      |{{/section}}
      |{{/data}}
      |""".trim.stripMargin
  val data = DefaultJSONReader.from

  {"data": [{"section: [{"name": "a", "value": 1}, {"name": "b", "value": 2}]}]}
   */
  println(processMustache(Map("qwer" -> List(Map("erty" -> 123), Map("erty" -> 456))), t))

}
