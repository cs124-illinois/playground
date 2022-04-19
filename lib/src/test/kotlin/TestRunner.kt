@file:Suppress("SpellCheckingInspection")

import edu.illinois.cs.cs124.playground.Submission
import edu.illinois.cs.cs124.playground.run
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class TestRunner : StringSpec({
    "it should stop a spinning container" {
        Submission(
            "cs124/playground-runner-python",
            listOf(
                Submission.FakeFile(
                    "main.py",
                    """
                |while True:
                |  print("Hello, Python!")
                    """.trimMargin()
                )
            ),
            1000L
        ).run().apply {
            timedOut shouldBe true
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
            |}
                    """.trimMargin()
                )
            ),
            timeout = 4000L
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, CPP!"
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
    "it should run a Haskell container" {
        Submission(
            "cs124/playground-runner-haskell",
            listOf(
                Submission.FakeFile(
                    "main.hs",
                    """
                    |main = putStrLn "Hello, Haskell!"
                    """.trimMargin()
                )
            ),
            timeout = 8000L
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, Haskell!"
        }
    }
    "it should run a Java container" {
        Submission(
            "cs124/playground-runner-java",
            listOf(
                Submission.FakeFile(
                    "Main.java",
                    """
                    |public class Main {
                    |  public static void main(String[] unused) {
                    |    System.out.println("Hello, Java!");
                    |  }
                    |}
                    """.trimMargin()
                )
            ),
            timeout = 4000L
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, Java!"
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
    "it should run a Kotlin container" {
        Submission(
            "cs124/playground-runner-kotlin",
            listOf(
                Submission.FakeFile(
                    "Main.kt",
                    """
                    |fun main() {
                    |  println("Hello, Kotlin!")
                    |}
                    """.trimMargin()
                )
            ),
            timeout = 8000L
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, Kotlin!"
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
    "it should run a Rust container" {
        Submission(
            "cs124/playground-runner-rust",
            listOf(
                Submission.FakeFile(
                    "main.rs",
                    """
                    |fn main() {
                    |  println!("Hello, Rust!");
                    |}
                    """.trimMargin()
                )
            ),
            timeout = 4000L
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, Rust!"
        }
    }
    "it should run a Scala3 container" {
        Submission(
            "cs124/playground-runner-scala3",
            listOf(
                Submission.FakeFile(
                    "Main.sc",
                    """
                    |object Main {
                    |  def main(args: Array[String]) = {
                    |    println("Hello, Scala!")
                    |  }
                    |}
                    """.trimMargin()
                )
            ),
            timeout = 8000L
        ).run().apply {
            timedOut shouldBe false
            output shouldBe "Hello, Scala!"
        }
    }
})
