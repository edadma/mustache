package io.github.edadma

package object mustache {

  private val defaults = Map("start" -> "{{", "end" -> "}}", "miss" -> "empty")

  def apply(data: Map[String, Any], template: String, options: (String, Any)*): String = {
    val config = defaults ++ options

    Renderer.render(data, Parser.parse(template, config), config)
  }

}
