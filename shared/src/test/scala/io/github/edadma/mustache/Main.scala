package io.github.edadma.mustache

object Main extends App {

  val t = "asdf {{qwer}} zxcv"

  println(Parser(null, t))

}
