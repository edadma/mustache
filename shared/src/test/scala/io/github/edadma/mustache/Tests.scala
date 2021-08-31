package io.github.edadma.mustache

import io.github.edadma.json.DefaultJSONReader
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class Tests extends AnyFreeSpec with Matchers {

  "empty" in {
    processMustache(Map(), "") shouldBe ""
  }

  "simple string" in {
    processMustache(Map(), "asdf") shouldBe "asdf"
  }

  "lines with spaces and blanks" in {
    processMustache(Map(), " asdf\n\nqwer \n") shouldBe "asdf\nqwer"
  }

  "lines with spaces and blanks (no trim)" in {
    processMustache(Map(), " asdf\n\nqwer \n", "trim" -> false) shouldBe " asdf\nqwer "
  }

  "simple variable" in {
    processMustache(Map("asdf" -> 345), "qwer {{asdf}} zxcv") shouldBe "qwer 345 zxcv"
  }

  "variable miss" in {
    processMustache(
      Map("name" -> "Chris", "company" -> "<b>GitHub</b>"),
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

}
