package io.github.edadma.mustache

object Main extends App {

  val t = "asdf {{#qwer}}{{erty}}{{/qwer}} zxcv"

  println(apply(Map("qwer" -> List(Map("erty" -> 123), Map("erty" -> 456))), t))

}
