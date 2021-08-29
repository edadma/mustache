package io.github.edadma.mustache

object Main extends App {

  val t = "asdf {{#qwer}}fghj{{/qwer}} zxcv"

  println(apply(Map("qwer" -> Map("erty" -> 123)), t))

}
