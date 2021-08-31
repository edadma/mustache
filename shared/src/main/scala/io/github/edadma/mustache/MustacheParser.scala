package io.github.edadma.mustache

import io.github.edadma.char_reader.CharReader

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

object MustacheParser {

  def parse(template: String, config: Map[String, Any]): AST = {
    @tailrec
    def matchTag(r: CharReader, buf: StringBuilder = new StringBuilder): Option[(CharReader, String)] = {
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

    def parse(r: CharReader,
              body: Option[(String, CharReader)],
              buf: StringBuilder = new StringBuilder,
              seq: ListBuffer[AST] = new ListBuffer): (CharReader, AST) = {
      if (r.more) {
        matches(r, config("start").toString) match {
          case Some(tagrest) =>
            if (buf.nonEmpty) {
              seq += TextAST(buf.toString)
              buf.clear()
            }

            matchTag(tagrest) match {
              case Some((rest, s)) =>
                def ref(s: String) =
                  if (s.startsWith(".") || s.endsWith(".") || s.contains("..")) r.error(s"bad variable name in tag: $s")
                  else s.split("\\.").toList

                val tag =
                  s.trim split "\\s+" match {
                    case Array(v)    => (v.takeWhile(!_.isLetter), v.dropWhile(!_.isLetter))
                    case Array(c, a) => (c, a)
                    case _           => tagrest.error(s"bad tag: $s")
                  }

                tag match {
                  case ("/", v) =>
                    if (body.isEmpty)
                      r.error(s"end tag with no start tag: $v")

                    val res =
                      if (seq.length == 1) seq.head
                      else SequenceAST(seq.toList)

                    (rest, res)
                  case _ =>
                    val rest1 =
                      tag match {
                        case ("", v) =>
                          seq += VariableAST(r, ref(v))
                          rest
                        case ("&", v) =>
                          seq += UnescapedAST(r, ref(v))
                          rest
                        case ("#", v) =>
                          val (rest1, ast) = parse(rest, body = Some((v, r)))

                          seq += SectionAST(r, ref(v), ast)
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
          seq += TextAST(buf.toString)

        if (seq.length == 1) seq.head
        (r, SequenceAST(seq.toList))
      }
    }

    val (_, res) = parse(CharReader.fromString(template), body = None)

    res
  }

}
