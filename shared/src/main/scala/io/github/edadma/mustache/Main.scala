package io.github.edadma.mustache

import scopt.OParser

import io.github.edadma.json.DefaultJSONReader

object Main extends App {
  case class Config(file: String, encoding: String)

  val builder = OParser.builder[Config]
  val parser = {
    import builder._

    OParser.sequence(
      programName("mustache"),
      head("Mustache", "v0.1.12"),
      help('h', "help").text("prints this usage text"),
      opt[String]('e', "encoding")
        .optional()
        .action((e, c) => c.copy(encoding = e))
        .validate(e =>
          if (e == "UTF-8") success
          else failure(s"invalid character encoding scheme: $e"),
        )
        .text("set character encoding scheme"),
      version('v', "version").text("prints the version"),
      arg[String]("<data>")
        .action((f, c) => c.copy(file = f))
//        .validate(f =>
//          if (!f.exists || f.isFile && f.canRead) success
//          else failure("<file> must be a readable file if it exists"))
        .text("path to text file to open"),
    )
  }

//  OParser.parse(parser, args, Config("untitled", "UTF-8")) match {
//    case Some(conf) => app(conf)
//    case _          =>
//  }
//
//  def app(conf: Config): Unit = {}
}
