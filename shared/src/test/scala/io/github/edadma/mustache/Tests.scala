package io.github.edadma.mustache

import io.github.edadma.json.DefaultJSONReader
import io.github.edadma.json

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class Tests extends AnyFreeSpec with Matchers {

  "empty" in {
    processMustache(json.Object(), "") shouldBe ""
  }

  "simple string" in {
    processMustache(json.Object(), "asdf") shouldBe "asdf"
  }

  "lines with spaces and blanks" in {
    processMustache(json.Object(), " asdf\n\nqwer \n") shouldBe "asdf\nqwer"
  }

  "lines with spaces and blanks (no trim)" in {
    processMustache(json.Object(), " asdf\n\nqwer \n", options = List("trim" -> false)) shouldBe " asdf\nqwer "
  }

  "simple variable" in {
    processMustache(json.Object("asdf" -> 345), "qwer {{asdf}} zxcv") shouldBe "qwer 345 zxcv"
  }

  "missing variable" in {
    processMustache(
      json.Object("name" -> "Chris", "company" -> "<b>GitHub</b>"),
      """
            |* {{name}}
            |* {{age}}
            |* {{company}}
            |* {{& company}}
            |""".trim.stripMargin,
    ) shouldBe
      """
        |* Chris
        |*
        |* &lt;b&gt;GitHub&lt;/b&gt;
        |* <b>GitHub</b>
        """.trim.stripMargin
  }

  "sections: non-empty lists" in {
    processMustache(
      DefaultJSONReader.fromString("""
                                     |{
                                     |  "repo": [
                                     |    { "name": "resque" },
                                     |    { "name": "hub" },
                                     |    { "name": "rip" }
                                     |  ]
                                     |}
                                   """.stripMargin),
      """
        |{{#repo}}
        |  <b>{{name}}</b>
        |{{/repo}}
        """.trim.stripMargin,
    ) shouldBe
      """
        |<b>resque</b>
        |<b>hub</b>
        |<b>rip</b>
        """.trim.stripMargin
  }

  "sections: false values or empty lists" in {
    processMustache(
      DefaultJSONReader.fromString("""
          |{
          |  "person": false
          |}
          """.stripMargin),
      """
        |Shown.
        |{{#person}}
        |  Never shown!
        |{{/person}}
        |""".trim.stripMargin,
    ) shouldBe
      """
        |Shown.
        """.trim.stripMargin
  }

  "sections: non-false values" in {
    processMustache(
      DefaultJSONReader.fromString("""
                                     |{
                                     |  "person?": { "name": "Jon" }
                                     |}
          """.stripMargin),
      """
        |{{#person?}}
        |  Hi {{name}}!
        |{{/person?}}
        |""".trim.stripMargin,
    ) shouldBe
      """
        |Hi Jon!
        """.trim.stripMargin
  }

  "sections: missing variable" in {
    processMustache(
      DefaultJSONReader.fromString("""
                                     |{
                                     |  
                                     |}
          """.stripMargin),
      """
        |{{#person?}}
        |  Hi {{name}}!
        |{{/person?}}
        |""".trim.stripMargin,
    ) shouldBe
      """
        |Hi !
        """.trim.stripMargin
  }

  "inverted sections" in {
    processMustache(
      DefaultJSONReader.fromString("""
                                     |{
                                     |  "repo": []
                                     |}
          """.stripMargin),
      """
        |{{#repo}}
        |  <b>{{name}}</b>
        |{{/repo}}
        |{{^repo}}
        |  No repos :(
        |{{/repo}}
        |""".trim.stripMargin,
    ) shouldBe
      """
        |No repos :(
        """.trim.stripMargin
  }

  "immediate parent" in {
    processMustache(
      DefaultJSONReader.fromString("""
                                     |{
                                     |  "a": {
                                     |    "b": 3
                                     |  },
                                     |  "c": 4
                                     |}
                                    """.stripMargin),
      """
        |{{#a}}
        |a.b: {{b}} c: {{_.c}}
        |{{/a}}
        |""".trim.stripMargin,
    ) shouldBe
      """
        |a.b: 3 c: 4
        """.trim.stripMargin
  }

  "parent through list" in {
    processMustache(
      DefaultJSONReader.fromString("""
                                     |{
                                     |  "repo": [
                                     |    { "name": "resque" },
                                     |    { "name": "hub" },
                                     |    { "name": "rip" }
                                     |  ],
                                     |  "asdf": 123
                                     |}
                                    """.stripMargin),
      """
        |{{#repo}}
        |  {{name}}, {{_._.asdf}}
        |{{/repo}}
      """.trim.stripMargin,
    ) shouldBe
      """
        |resque, 123
        |hub, 123
        |rip, 123
      """.trim.stripMargin
  }

  "partials" in {
    processMustache(
      DefaultJSONReader.fromString("""
                                     |{
                                     |  "names": [
                                     |    { "name": "resque" },
                                     |    { "name": "hub" },
                                     |    { "name": "rip" }
                                     |  ]
                                     |}
                                    """.stripMargin),
      """
        |<h2>Names</h2>
        |{{#names}}
        |  {{> user}}
        |{{/names}}
      """.trim.stripMargin,
      predefs = List("user" -> MustacheParser.parse("<strong>{{name}}</strong>", defaultOptions)),
    ) shouldBe
      """
        |<h2>Names</h2>
        |<strong>resque</strong>
        |<strong>hub</strong>
        |<strong>rip</strong>
      """.trim.stripMargin
  }

  "dotted names" in {
    processMustache(
      DefaultJSONReader.fromString("""
                                     |{
                                     |  "name": {
                                     |    "first": "Simon",
                                     |    "last": "Haley"
                                     |  },
                                     |  "age": "73"
                                     |}
                                    """.stripMargin),
      """
        |* {{name.first}} {{name.last}}
        |* {{age}}
      """.trim.stripMargin,
    ) shouldBe
      """
        |* Simon Haley
        |* 73
      """.trim.stripMargin
  }

}
