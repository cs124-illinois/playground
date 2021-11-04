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
        Submission(
            "cs124/playground-python",
            listOf(Submission.FakeFile("main.py", """print("Hello, Python!")"""))
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, Python!"
        }
    }
    "it should stop a spinning Python container" {
        Submission(
            "cs124/playground-python",
            listOf(
                Submission.FakeFile(
                    "main.py",
                    """
            |while True:
            |    i = 0
        """.trimMargin()
                )
            ),
            1000
        ).run().apply {
            timedOut shouldBe true
        }
    }
    "it should run a CPP container" {
        Submission(
            "cs124/playground-cpp",
            listOf(
                Submission.FakeFile(
                    "main.cpp",
                    """
            |#include <iostream>
            |int main() {
            |  std::cout << "Hello, CPP!\n";
            |  return 0;
            |}""".trimMargin()
                )
            )
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, CPP!"
        }
    }
    "it should stop a spinning CPP container" {
        Submission(
            "cs124/playground-cpp",
            listOf(
                Submission.FakeFile(
                    "main.cpp",
                    """
            |#include <iostream>
            |int main() {
            |  while (1) { }
            |  return 0;
            |}""".trimMargin()
                )
            ),
            1000
        ).run().apply {
            timedOut shouldBe true
        }
    }
    "it should run a Julia container" {
        Submission(
            "cs124/playground-julia",
            listOf(
                Submission.FakeFile(
                    "main.jl",
                    """print("Hello, Julia!")"""
                )
            )
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, Julia!"
        }
    }
    "it should run a R container" {
        Submission(
            "cs124/playground-r",
            listOf(
                Submission.FakeFile(
                    "main.R",
                    """cat("Hello, R!")"""
                )
            )
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, R!"
        }
    }
    "it should run a C container" {
        Submission(
            "cs124/playground-c",
            listOf(
                Submission.FakeFile(
                    "main.c",
										"""
                    |#include <stdio.h>
                    |int main () {
                    |   printf("Hello, C!\\n");
                    |   return 0;
                    |}
                    """.trimMargin()
                )
            )
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, C!"
        }
    }
})
