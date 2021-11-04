@file:Suppress("SpellCheckingInspection")

import edu.illinois.cs.cs124.playground.Submission
import edu.illinois.cs.cs124.playground.run
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestRunner : StringSpec({
    "it should run a helloworld container" {
        Submission("cs124/playground-test-helloworld", timeout = 1000L).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, world!"
        }
    }
    "it should stop a spinning container" {
        Submission("cs124/playground-test-spinner", timeout = 1000L).run().apply {
            timedOut shouldBe true
        }
    }
    "it should run a Python container" {
        Submission(
            "cs124/playground-runner-python",
            listOf(Submission.FakeFile("main.py", """print("Hello, Python!")""")),
            4000L
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, Python!"
        }
    }
    "it should run a CPP container" {
        Submission(
            "cs124/playground-runner-cpp",
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
            ),
            timeout = 4000L
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, CPP!"
        }
    }
    "it should run a Julia container" {
        Submission(
            "cs124/playground-runner-julia",
            listOf(
                Submission.FakeFile(
                    "main.jl",
                    """print("Hello, Julia!")"""
                )
            ),
            timeout = 4000L
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, Julia!"
        }
    }
    "it should run a R container" {
        Submission(
            "cs124/playground-runner-r",
            listOf(
                Submission.FakeFile(
                    "main.R",
                    """cat("Hello, R!")"""
                )
            ),
            timeout = 4000L
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, R!"
        }
    }
    "it should run a C container" {
        Submission(
            "cs124/playground-runner-c",
            listOf(
                Submission.FakeFile(
                    "main.c",
                    """
                    |#include <stdio.h>
                    |int main () {
                    |   printf("Hello, C!\n");
                    |   return 0;
                    |}
                    |
                    """.trimMargin()
                )
            ),
            timeout = 4000L
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, C!"
        }
    }
    "it should run a Go container" {
        Submission(
            "cs124/playground-runner-go",
            listOf(
                Submission.FakeFile(
                    "main.go",
                    """
                    |package main
                    |import "fmt"
                    |func main() {
                    |  fmt.Println("Hello, Go!")
                    |}
                    """.trimMargin()
                )
            ),
            timeout = 4000L
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, Go!"
        }
    }
})
