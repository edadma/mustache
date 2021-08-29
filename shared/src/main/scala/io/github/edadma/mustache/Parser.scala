package io.github.edadma.mustache

import io.github.edadma.char_reader.CharReader

import scala.annotation.tailrec

object Parser {

  val default = Map("start" -> "{{", "end" -> "}}")

  def apply(data: OBJ, template: String, options: (String, Any)*): AST = parse(data, template, default ++ options)

  def parse(data: OBJ, template: String, config: OBJ): AST = {
    @tailrec
    def tag(r: CharReader, buf: StringBuilder = new StringBuilder): Option[(CharReader, String)] = {
      if (r.eoi) None
      else {
        matches(r, config("end").toString) match {
          case Some(rest) => Some((rest, buf.toString))
          case None =>
            buf += r.ch
            tag(r.next, buf)
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
    def parse(r: CharReader): Unit =
      if (r.more)
        matches(r, config("start").toString) match {
          case Some(tagrest) =>
            tag(tagrest) match {
              case Some((rest, s)) =>
                s match {
                  case _ =>
                    println(tagrest.longErrorText(s))
                    parse(rest)
                  case _ => tagrest.error(s"bad tag: $s")
                }
              case None =>
            }
          case None =>
            if (r.more) {
              buf += r.ch
              parse(r.next)
            }
        }

    def body(): AST = {
      if (seq.)
    }

    parse(CharReader.fromString(template))
    buf.toString
  }

}
