package io.github.edadma.mustache

import io.github.edadma.char_reader.CharReader
import org.graalvm.compiler.lir.LIRInstruction.Temp

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

object Parser {

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

    @tailrec
    def parse(r: CharReader, buf: StringBuilder = new StringBuilder, seq: ListBuffer[AST] = new ListBuffer): AST = {
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
                    case Array(v) =>
                      if (!v.head.isLetter) (v.head.toString, v.tail)
                      else ("", v)
                    case Array(c, a) => (c, a)
                    case _           => tagrest.error(s"bad tag: $s")
                  }

                tag match {
                  case ("", v)  => seq += VariableAST(r, ref(v))
                  case ("#", v) =>
                  case (c, _)   => tagrest.error(s"unrecognized tag command: $c")
                }

                parse(rest, buf, seq)
              case None => r.error("unclosed tag")
            }
          case None =>
            buf += r.ch
            parse(r.next, buf, seq)
        }
      } else {
        if (buf.nonEmpty)
          seq += TextAST(buf.toString)

        if (seq.length == 1) seq.head
        SequenceAST(seq.toList)
      }
    }

    parse(CharReader.fromString(template))
  }

}
