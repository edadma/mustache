package io.github.edadma.mustache

import io.github.edadma.json.{DefaultJSONReader, Obj}

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class Tests extends AnyFreeSpec with Matchers {

  "empty" in {
    processMustache(Obj(), "") shouldBe ""
  }

  "simple string" in {
    processMustache(Obj(), "asdf") shouldBe "asdf"
  }

  "lines with spaces and blanks" in {
    processMustache(Obj(), " asdf\n\nqwer \n") shouldBe "asdf\nqwer"
  }

  "lines with spaces and blanks (no trim)" in {
    processMustache(Obj(), " asdf\n\nqwer \n", "trim" -> false) shouldBe " asdf\nqwer "
  }

  "simple variable" in {
    processMustache(Obj("asdf" -> 345), "qwer {{asdf}} zxcv") shouldBe "qwer 345 zxcv"
  }

  "missing variable" in {
    processMustache(
      Obj("name" -> "Chris", "company" -> "<b>GitHub</b>"),
      """
            |* {{name}}
            |* {{age}}
            |* {{company}}
            |* {{& company}}
            |""".trim.stripMargin
    ) shouldBe
      """
        |* Chris
        |*
        |* &lt;b&gt;GitHub&lt;/b&gt;
        |* <b>GitHub</b>
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
        |""".trim.stripMargin
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
        |""".trim.stripMargin
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
        |""".trim.stripMargin
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
        |""".trim.stripMargin
    ) shouldBe
      """
        |No repos :(
        """.trim.stripMargin
  }

  "parent variable" in {
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
        |""".trim.stripMargin
    ) shouldBe
      """
        |a.b: 3 c: 4
        """.trim.stripMargin
  }

}
