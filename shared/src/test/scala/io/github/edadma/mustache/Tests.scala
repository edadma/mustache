package io.github.edadma.mustache

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
    processMustache(Map(), " asdf\n\nqwer \n", "trim" -> false) shouldBe " asdf\n\nqwer \n"
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
            |""".stripMargin
    ) shouldBe
      """
        |* Chris
        |*
        |* &lt;b&gt;GitHub&lt;/b&gt;
        |* <b>GitHub</b>
        """.trim.stripMargin
  }

}
