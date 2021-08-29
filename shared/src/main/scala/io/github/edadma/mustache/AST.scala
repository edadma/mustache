package io.github.edadma.mustache

trait AST
case class TextAST(s: String) extends AST
case class SectionAST(id: List[String], contents: AST) extends AST
case class InvertedAST(id: List[String], contents: AST) extends AST
case class PartialAST(file: String) extends AST
