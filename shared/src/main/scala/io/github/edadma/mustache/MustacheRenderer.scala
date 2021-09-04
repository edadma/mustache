package io.github.edadma.mustache

import io.github.edadma.char_reader.CharReader
import io.github.edadma.{cross_platform, json}

import scala.annotation.tailrec

import scala.collection.mutable

object MustacheRenderer {

  def render(data: Any,
             template: AST,
             config: Map[String, Any],
             paths: Map[String, String] = Map(),
             predefs: List[(String, AST)] = Nil): String = {
    val buf = new StringBuilder
    val partials = mutable.HashMap[String, AST]() ++= predefs
    val htmlEscapedOpt = config("htmlEscaped").asInstanceOf[Boolean]
    val trimOpt = config("trim").asInstanceOf[Boolean]
    val removeSectionBlanksOpt = config("removeSectionBlanks").asInstanceOf[Boolean]
    val removeNonSectionBlanksOpt = config("removeNonSectionBlanks").asInstanceOf[Boolean]
    val missingIsException = config("missingIsException").asInstanceOf[Boolean]

    @tailrec
    def lookup(data: Any, pos: CharReader, id: List[String]): Any = {
      def missing(name: String): String =
        if (missingIsException) pos.error(s"missing variable '$name'")
        else ""

      (data, id) match {
        case (o: json.Aggregate, "_" :: tl) =>
          o.parent match {
            case null => pos.error(s"object has no parent: $o")
            case p    => lookup(p, pos, tl)
          }
        case (o: json.Object, hd :: tl) =>
          o get hd match {
            case Some(value) => lookup(value, pos, tl)
            case None        => missing(hd)
          }
        case (_, hd :: _) => missing(hd)
        case (null, _)    => ""
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
        case a: json.Array =>
          for (d <- a) {
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
            case DataAST =>
              append(data.toString)
              section = false
              nl = false
            case VariableAST(pos, id) =>
//              print(s"<${lookup(data, pos, id).toString}>")
              append(lookup(data, pos, id).toString)
              section = false
              nl = false
            case UnescapedAST(pos, id) =>
              buf ++= lookup(data, pos, id).toString
              section = false
              nl = false
            case PartialAST(pos, file) =>
              val partial =
                paths get file match {
                  case Some(p) => p
                  case None    => file
                }

              render(
                data,
                partials get file match {
                  case Some(ast) => ast
                  case None =>
                    val ast =
                      MustacheParser.parse(cross_platform.readFile(s"$partial.mustache"), config)

                    partials(file) = ast
                    ast
                }
              )

              section = false
              nl = false
            case SectionAST(pos, id, body) =>
              section = true

              val v = lookup(data, pos, id)

              if (!(v == false || v.isInstanceOf[json.Array] && v.asInstanceOf[json.Array].isEmpty))
                render(v, body)

              section = true
            case InvertedSectionAST(pos, id, body) =>
              section = true

              val v = lookup(data, pos, id)

              if (v == "" || v == false || v.isInstanceOf[json.Array] && v.asInstanceOf[json.Array].isEmpty)
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
