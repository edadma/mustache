package io.github.edadma.mustache

trait AST
case class TextAST(s: String) extends AST
case class VariableAST(id: List[String]) extends AST
case class SectionAST(id: List[String], body: AST) extends AST
case class InvertedAST(id: List[String], body: AST) extends AST
case class PartialAST(file: String) extends AST
case class SequenceAST(contents: List[AST]) extends AST
