package io.github.edadma.mustache

import io.github.edadma.char_reader.CharReader

trait AST
case class TextAST(s: String) extends AST
case object NewlineAST extends AST
case class VariableAST(pos: CharReader, id: List[String]) extends AST
case class UnescapedAST(pos: CharReader, id: List[String]) extends AST
case class SectionAST(pos: CharReader, id: List[String], body: AST) extends AST
case class InvertedAST(pos: CharReader, id: List[String], body: AST) extends AST
case class PartialAST(file: String) extends AST
case class SequenceAST(contents: List[AST]) extends AST
