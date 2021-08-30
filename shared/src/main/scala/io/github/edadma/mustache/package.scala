package io.github.edadma

package object mustache {

  private val defaults =
    Map("start" -> "{{",
        "end" -> "}}",
        "miss" -> "empty",
        "trim" -> true,
        "removeSectionBlanks" -> true,
        "removeNonSectionBlanks" -> true,
        "htmlEscaped" -> true)

  def processMustache(data: Any, template: String, options: (String, Any)*): String = {
    val config = defaults ++ options

    MustacheRenderer.render(data, MustacheParser.parse(template, config), config)
  }

}
