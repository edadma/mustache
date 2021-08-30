package io.github.edadma.mustache

object Main extends App {

  val t =
    """
      |asdf 
      |{{#x}} zxcv
      | {{/x}} qwer""".stripMargin

  println(processMustache(Map("qwer" -> List(Map("erty" -> 123), Map("erty" -> 456))), t))

}
