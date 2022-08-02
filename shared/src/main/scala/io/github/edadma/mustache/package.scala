package io.github.edadma

package object mustache {

  private[mustache] val defaultOptions =
    Map(
      "start" -> "{{",
      "end" -> "}}",
      "miss" -> "empty",
      "trim" -> true,
      "removeSectionBlanks" -> true,
      "removeNonSectionBlanks" -> true,
      "htmlEscaped" -> true,
      "missingIsException" -> false,
    )

  def processMustache(
      data: Any,
      template: String,
      paths: Map[String, String] = Map(),
      predefs: List[(String, AST)] = Nil,
      options: List[(String, Any)] = Nil,
  ): String = {
    val config = defaultOptions ++ options

    MustacheRenderer.render(data, MustacheParser.parse(template, config), config, paths, predefs)
  }

}
