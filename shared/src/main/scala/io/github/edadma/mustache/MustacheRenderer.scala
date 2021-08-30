package io.github.edadma.mustache

import io.github.edadma.char_reader.CharReader

import scala.annotation.tailrec

object MustacheRenderer {

  def render(data: Any, template: AST, config: Map[String, Any]): String = {
    val buf = new StringBuilder
    val htmlEscapedOpt = config("htmlEscaped").asInstanceOf[Boolean]
    val trimOpt = config("trim").asInstanceOf[Boolean]
    val removeSectionBlanksOpt = config("removeSectionBlanks").asInstanceOf[Boolean]
    val removeNonSectionBlanksOpt = config("removeNonSectionBlanks").asInstanceOf[Boolean]

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
      if (htmlEscapedOpt) {
        buf ++= (s flatMap {
          case '&' => "&amp;"
          case '<' => "&lt;"
          case '>' => "&gt;"
          case c   => c.toString
        })
      } else
        buf ++= s

    var section: Boolean = false

    def render(data: Any, template: AST): Unit =
      data match {
        case l: List[_] => l foreach (d => render(d, template))
        case _ =>
          template match {
            case TextAST(s) =>
              if (!(section && removeSectionBlanksOpt && s.forall(_ == '\n')))
                buf ++= s

              section = false
            case VariableAST(pos, id)  => append(lookup(data, pos, id).toString)
            case UnescapedAST(pos, id) => buf ++= lookup(data, pos, id).toString
            case SectionAST(pos, id, body) =>
              section = true
              render(lookup(data, pos, id), body)
            //        case InvertedAST(id, body) =>
            //        case PartialAST(file) =>
            case SequenceAST(contents) => contents foreach (t => render(data, t))
          }

          template match {
            case _: SectionAST | _: InvertedAST => section = true
            case _                              =>
          }
      }

    render(data, template)
    scala.io.Source.fromString(buf.toString).getLines() map (l => if (trimOpt) l.trim else l) filterNot (l =>
      removeNonSectionBlanksOpt && l.isEmpty) mkString "\n"
  }

}
