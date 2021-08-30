package io.github.edadma

package object mustache {

  private val defaults =
    Map("start" -> "{{", "end" -> "}}", "miss" -> "empty", "trim" -> true, "htmlEscaped" -> true)

  def processMustache(data: Map[String, Any], template: String, options: (String, Any)*): String = {
    val config = defaults ++ options

    MustacheRenderer.render(data, MustacheParser.parse(template, config), config)
  }

}
