package io.github.edadma

package object mustache {

  private[mustache] val defaultsOptions =
    Map(
      "start" -> "{{",
      "end" -> "}}",
      "miss" -> "empty",
      "trim" -> true,
      "removeSectionBlanks" -> true,
      "removeNonSectionBlanks" -> true,
      "htmlEscaped" -> true,
      "missingIsException" -> false
    )

  def processMustache(data: Any, template: String, options: (String, Any)*): String = {
    val config = defaultsOptions ++ options

    MustacheRenderer.render(data, MustacheParser.parse(template, config), config)
  }

}
