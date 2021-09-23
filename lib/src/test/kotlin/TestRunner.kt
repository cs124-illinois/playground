@file:Suppress("SpellCheckingInspection")

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestRunner : StringSpec({
    "it should run a helloworld container" {
        run("cs124/helloworld").also {
            it.timedOut shouldBe false
            it.output shouldBe "Hello, world!"
        }
    }
    "it should stop a spinning container" {
        run("cs124/spinner", 1000).also {
            it.timedOut shouldBe true
        }
    }
    "it should run a Python container" {
        run("cs124/python", 1000, mapOf("main.py" to """print("Hello, Python!")""")).also {
            it.timedOut shouldBe false
            it.output shouldBe "Hello, Python!"
        }
    }
    "it should stop a spinning Python container" {
        run(
            "cs124/python", 1000,
            mapOf(
                "main.py" to """
            |while True:
            |    i = 0
        """.trimMargin()
            )
        ).also {
            it.timedOut shouldBe true
        }
    }
    "it should run a CPP container" {
        run(
            "cs124/cpp", 1000,
            mapOf(
                "main.cpp" to """
            |#include <iostream>
            |int main() {
            |  std::cout << "Hello, CPP!\n";
            |  return 0;
            |}""".trimMargin()
            )
        ).also {
            it.timedOut shouldBe false
            it.output shouldBe "Hello, CPP!"
        }
    }
    "it should stop a spinning CPP container" {
        run(
            "cs124/cpp", 1000,
            mapOf(
                "main.cpp" to """
            |#include <iostream>
            |int main() {
            |  while (1) { }
            |  return 0;
            |}""".trimMargin()
            )
        ).also {
            it.timedOut shouldBe true
        }
    }
})
