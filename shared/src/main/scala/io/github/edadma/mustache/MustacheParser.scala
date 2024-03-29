package io.github.edadma.mustache

import io.github.edadma.char_reader.CharReader

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object MustacheParser {

  private[mustache] def text(s: String): List[AST] = {
    require(s.nonEmpty)

    val buf = new ListBuffer[AST]

    def nls(s: String): Unit = buf ++= Seq.fill(s.count(_ == '\n'))(NewlineAST)

    def isNewline(c: Char) = c == '\n' || c == '\r'

    @tailrec
    def text(from: Int): Unit =
      if (isNewline(s(from)))
        s.indexWhere(c => !isNewline(c), from) match {
          case -1 => nls(s.substring(from, s.length))
          case totext =>
            nls(s.substring(from, totext))
            text(totext)
        }
      else {
        s.indexWhere(isNewline, from) match {
          case -1 => buf += TextAST(s.substring(from, s.length))
          case tonl =>
            buf += TextAST(s.substring(from, tonl))
            text(tonl)
        }
      }

    text(0)
    buf.toList
  }

  def parse(template: String, config: Map[String, Any]): AST = {
    @tailrec
    def matchTag(
        r: CharReader,
        buf: mutable.StringBuilder = new mutable.StringBuilder,
    ): Option[(CharReader, String)] = {
      if (r.eoi) None
      else {
        matches(r, config("end").toString) match {
          case Some(rest) => Some((rest, buf.toString))
          case None =>
            buf += r.ch
            matchTag(r.next, buf)
        }
      }
    }

    def matches(r: CharReader, s: String): Option[CharReader] = {
      @tailrec
      def matches(r: CharReader, s: List[Char]): Option[CharReader] =
        s match {
          case head :: tail =>
            if (head == r.ch) matches(r.next, tail)
            else None
          case Nil => Some(r)
        }

      matches(r, s.toList)
    }

    def isNameChar(c: Char) = c.isLetter || c == '_'

    def parse(
        r: CharReader,
        body: Option[(String, CharReader)],
        buf: mutable.StringBuilder = new mutable.StringBuilder,
        seq: ListBuffer[AST] = new ListBuffer,
    ): (CharReader, AST) = {
      if (r.more) {
        matches(r, config("start").toString) match {
          case Some(tagrest) =>
            if (buf.nonEmpty) {
              seq ++= text(buf.toString)
              buf.clear()
            }

            matchTag(tagrest) match {
              case Some((rest, s)) =>
                def ref(s: String) =
                  if (s.startsWith(".") || s.endsWith(".") || s.contains("..")) r.error(s"bad variable name in tag: $s")
                  else s.split("\\.").toList

                val tag =
                  s.trim split "\\s+" match {
                    case Array(v)    => (v.takeWhile(!isNameChar(_)), v.dropWhile(!isNameChar(_)))
                    case Array(c, a) => (c, a)
                    case _           => tagrest.error(s"bad tag: $s")
                  }

                tag match {
                  case ("/", v) =>
                    if (body.isEmpty)
                      tagrest.error(s"end tag with no start tag: $v")

                    val res =
                      if (seq.length == 1) seq.head
                      else SequenceAST(seq.toList)

                    (rest, res)
                  case _ =>
                    val rest1 =
                      tag match {
                        case ("", v) =>
                          seq += VariableAST(tagrest, ref(v))
                          rest
                        case (".", "") =>
                          seq += DataAST
                          rest
                        case (".", att) =>
                          seq += AttributeAST(tagrest, att)
                          rest
                        case ("&", v) =>
                          seq += UnescapedAST(tagrest, ref(v))
                          rest
                        case (">", v) =>
                          seq += PartialAST(tagrest, v)
                          rest
                        case ("#", v) =>
                          val (rest1, ast) = parse(rest, body = Some((v, tagrest)))

                          seq += SectionAST(tagrest, ref(v), ast)
                          rest1
                        case ("^", v) =>
                          val (rest1, ast) = parse(rest, body = Some((v, tagrest)))

                          seq += InvertedSectionAST(tagrest, ref(v), ast)
                          rest1
                        case ("!", _) => rest
                        case (c, _)   => tagrest.error(s"unrecognized tag command: $c")
                      }

                    parse(rest1, body, buf, seq)
                }
              case None => r.error("unclosed tag")
            }
          case None =>
            buf += r.ch
            parse(r.next, body, buf, seq)
        }
      } else {
        body foreach { case (v, r) => r.error(s"unclosed section: $v") }

        if (buf.nonEmpty)
          seq ++= text(buf.toString)

        if (seq.length == 1) seq.head
        (r, SequenceAST(seq.toList))
      }
    }

    val (_, res) = parse(CharReader.fromString(template), body = None)

    res
  }

}
