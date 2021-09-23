@file:Suppress("SpellCheckingInspection")

import edu.illinois.cs.cs124.playground.Submission
import edu.illinois.cs.cs124.playground.run
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestRunner : StringSpec({
    "it should run a helloworld container" {
        Submission("cs124/helloworld").run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, world!"
        }
    }
    "it should stop a spinning container" {
        Submission("cs124/spinner", timeout = 1000).run().apply {
            timedOut shouldBe true
        }
    }
    "it should run a Python container" {
        Submission("cs124/python", mapOf("main.py" to """print("Hello, Python!")""")).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, Python!"
        }
    }
    "it should stop a spinning Python container" {
        Submission(
            "cs124/python",
            mapOf(
                "main.py" to """
            |while True:
            |    i = 0
        """.trimMargin()
            ),
            1000
        ).run().apply {
            timedOut shouldBe true
        }
    }
    "it should run a CPP container" {
        Submission(
            "cs124/cpp",
            mapOf(
                "main.cpp" to """
            |#include <iostream>
            |int main() {
            |  std::cout << "Hello, CPP!\n";
            |  return 0;
            |}""".trimMargin()
            )
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, CPP!"
        }
    }
    "it should stop a spinning CPP container" {
        Submission(
            "cs124/cpp",
            mapOf(
                "main.cpp" to """
            |#include <iostream>
            |int main() {
            |  while (1) { }
            |  return 0;
            |}""".trimMargin()
            ),
            1000
        ).run().apply {
            timedOut shouldBe true
        }
    }
})
