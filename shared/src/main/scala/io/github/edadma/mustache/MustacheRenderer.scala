package io.github.edadma.mustache

import io.github.edadma.char_reader.CharReader
import io.github.edadma.json.Obj

import scala.annotation.tailrec

object MustacheRenderer {

  def render(data: Any, template: AST, config: Map[String, Any]): String = {
    val buf = new StringBuilder
    val htmlEscapedOpt = config("htmlEscaped").asInstanceOf[Boolean]
    val trimOpt = config("trim").asInstanceOf[Boolean]
    val removeSectionBlanksOpt = config("removeSectionBlanks").asInstanceOf[Boolean]
    val removeNonSectionBlanksOpt = config("removeNonSectionBlanks").asInstanceOf[Boolean]
    val missingIsException = config("missingIsException").asInstanceOf[Boolean]

    @tailrec
    def lookup(data: Any, pos: CharReader, id: List[String]): Any = {
      def missing(name: String): String =
        if (missingIsException) pos.error(s"missing variable $name")
        else ""

      (data, id) match {
        case (m: Obj, "_" :: tl) =>
          m.parent match {
            case null => pos.error(s"object has no parent: $m")
            case p    => lookup(p, pos, tl)
          }
        case (m: Obj, hd :: tl) =>
          m get hd match {
            case Some(value) => lookup(value, pos, tl)
            case None        => missing(hd)
          }
        case (_, hd :: _) => missing(hd)
        case (v, _)       => v
      }
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
    var nl: Boolean = false

    def render(data: Any, template: AST): Unit =
      data match {
        case l: List[_] =>
          for (d <- l) {
            render(d, template)
            section = true
          }
        case _ =>
          template match {
            case TextAST(s) =>
//              print(s"[$s]")
              buf ++= s
              section = false
              nl = false
            case NewlineAST =>
//              print(s"[\\n: section = $section]")
              if (removeSectionBlanksOpt && section) {
                // remove
              } else if (removeNonSectionBlanksOpt && !section && nl) {
//                print("(r)")
                // remove
              } else {
                buf += '\n'
              }

              nl = true
            case VariableAST(pos, id) =>
//              print(s"<${lookup(data, pos, id).toString}>")
              append(lookup(data, pos, id).toString)
              section = false
              nl = false
            case UnescapedAST(pos, id) =>
              buf ++= lookup(data, pos, id).toString
              section = false
              nl = false
            case SectionAST(pos, id, body) =>
              section = true

              lookup(data, pos, id) match {
                case false | Nil =>
                case v           => render(v, body)
              }

              section = true
            case InvertedSectionAST(pos, id, body) =>
              section = true

              val v = lookup(data, pos, id)

              if (v == "" || v == false || v == Nil)
                render(false, body)

              section = true
            //        case PartialAST(file) =>
            case SequenceAST(contents) => contents foreach (t => render(data, t))
          }
      }

    render(data, template)
    scala.io.Source.fromString(buf.toString).getLines() map (l => if (trimOpt) l.trim else l) mkString "\n"
  }

}
