package io.github.edadma.mustache

import io.github.edadma.char_reader.CharReader

import scala.annotation.tailrec

object Renderer {

  def render(data: Map[String, Any], template: AST, config: Map[String, Any]): String = {
    val buf = new StringBuilder

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

    def render(data: Any, template: AST): Unit =
      data match {
        case l: List[_] => l foreach (d => render(d, template))
        case _ =>
          template match {
            case TextAST(s)                => buf ++= s
            case VariableAST(pos, id)      => buf ++= lookup(data, pos, id).toString
            case SectionAST(pos, id, body) => render(lookup(data, pos, id), body)
            //        case InvertedAST(id, body) =>
            //        case PartialAST(file) =>
            case SequenceAST(contents) => contents foreach (t => render(data, t))
          }
      }

    render(data, template)
    buf.toString
  }

}
