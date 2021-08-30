package io.github.edadma.mustache

import io.github.edadma.char_reader.CharReader

import scala.annotation.tailrec

object MustacheRenderer {

  def render(data: Map[String, Any], template: AST, config: Map[String, Any]): String = {
    val buf = new StringBuilder
    val htmlEscaped = config("htmlEscaped").asInstanceOf[Boolean]

    @tailrec
    def lookup(data: Any, pos: CharReader, id: List[String]): Any =
      (data, id) match {
        case (_, Nil) => data
        case (m: Map[_, _], hd :: tl) =>
          m.asInstanceOf[Map[String, Any]] get hd match {
            case Some(value) => lookup(value, pos, tl)
            case None =>
              config("miss") match {
                case "empty" => ""
              }
          }
        case (v, hd :: _) => pos.error(s"key '$hd' not found in '$v'")
        case (v, _)       => v
      }

    def append(s: String): Unit =
      if (htmlEscaped) {
        buf ++= (s flatMap {
          case '&' => "&amp;"
          case '<' => "&lt;"
          case '>' => "&gt;"
          case c   => c.toString
        })
      } else
        buf ++= s

    def render(data: Any, template: AST): Unit =
      data match {
        case l: List[_] => l foreach (d => render(d, template))
        case _ =>
          template match {
            case TextAST(s)                => buf ++= s
            case VariableAST(pos, id)      => append(lookup(data, pos, id).toString)
            case UnescapedAST(pos, id)     => buf ++= lookup(data, pos, id).toString
            case SectionAST(pos, id, body) => render(lookup(data, pos, id), body)
            //        case InvertedAST(id, body) =>
            //        case PartialAST(file) =>
            case SequenceAST(contents) => contents foreach (t => render(data, t))
          }
      }

    render(data, template)

    val res = buf.toString

    if (config("trim").asInstanceOf[Boolean])
      scala.io.Source.fromString(res).getLines() map (_.trim) filterNot (_.isEmpty) mkString "\n"
    else res
  }

}
